package geotrellis.admin.server

import geotrellis.admin.server.util.AvroRegistrator
import geotrellis.proj4.{LatLng, WebMercator}
import geotrellis.raster._
import geotrellis.raster.mapalgebra.local._
import geotrellis.raster.render._
import geotrellis.admin.server.services._
import geotrellis.spark._
import geotrellis.spark.io.AttributeStore.Fields
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.vector.io.json.Implicits._
import geotrellis.vector.Polygon
import geotrellis.vector.reproject._
import geotrellis.admin.server.util._

import akka.actor._
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import com.typesafe.config.Config
import org.apache.spark.{SparkConf, SparkContext}
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

class GTAdminServiceActor(config: Config) extends Actor with GeotrellisIngestTestService {
  val conf = AvroRegistrator(new SparkConf()
    .setMaster(config.getString("spark.master"))
    .setAppName("IngestTest")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")
    .setJars(SparkContext.jarOfObject(this).toList)
  )

  implicit val sparkContext = new SparkContext(conf)

  override def actorRefFactory = context
  override def receive = runRoute(serviceRoute)
  override val staticPath = config.getString("geotrellis.static-path")

  val s3Bucket = config.getString("geotrellis.s3.bucket")
  val s3Key = config.getString("geotrellis.s3.key");

  override lazy val reader: LayerReader[LayerId] = S3LayerReader(s3Bucket, s3Key)
  override lazy val attributeStore: AttributeStore = S3AttributeStore(s3Bucket, s3Key)
  override def tileReader(id: LayerId): Reader[SpatialKey, Tile] = 
    S3ValueReader(attributeStore, id)

}

trait GeotrellisIngestTestService extends HttpService with CORSSupport {
  implicit val sparkContext: SparkContext

  implicit val executionContext = actorRefFactory.dispatcher

  def reader: LayerReader[LayerId]
  def attributeStore: AttributeStore
  def tileReader(id: LayerId): Reader[SpatialKey, Tile]

  val staticPath: String
  val baseZoomLevel = 4

  def layerId(layer: String): LayerId =
    LayerId(layer, baseZoomLevel)

  def getMetaData(id: LayerId): TileLayerMetadata[SpatialKey] =
    attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](id)

  def serviceRoute = cors {
    get {
      pathPrefix("gt") {
        pathPrefix("bounds")(bounds) ~
        pathPrefix("metadata")(metadata) ~
        pathPrefix("layers")(layers) ~
        pathPrefix("tms")(tms) ~
        pathPrefix("breaks")(breaks)
      } ~
      pathEndOrSingleSlash {
        getFromResource("geotrellis/viewer/index.html")
      } ~
      pathPrefix("") {
        getFromResourceDirectory("geotrellis/viewer")
      }
    }
  }

  case class LayerDescription(name: String, availableZooms: Seq[Int])
  object EndpointProtocol extends DefaultJsonProtocol {
    implicit val ldFormat = jsonFormat2(LayerDescription)
    implicit val gbFormat = jsonFormat4(GridBounds.apply)
  }

  def bounds = pathPrefix(Segment / IntNumber) { (layerName, zoom) =>
    import EndpointProtocol._
    val data = reader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](LayerId(layerName, zoom))
    val bounds = data.metadata.gridBounds
    complete(bounds)
  }


  def layers = {
    import EndpointProtocol._
    complete {
      attributeStore.layerIds.groupBy(_.name).mapValues(_.map(_.zoom).sorted).filter(l => !(l._2 contains 0) ).map(l => LayerDescription(l._1, l._2))
    }
  }

  def metadata = pathPrefix(Segment / IntNumber) { (layerName, zoom) =>
    complete {
      val layer = LayerId(layerName, zoom)
      val metad = getMetaData(layer)
      println(metad)
      metad
    }
  }

  def breaks = pathPrefix(Segment / IntNumber) { (layer, numBreaks) =>
    import DefaultJsonProtocol._

    val data = (reader
      .read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](layerId(layer))
    )

    val doubleBreaks = data.classBreaksDouble(numBreaks)

    complete(JsObject("classBreaks" -> doubleBreaks.toJson))
  }

  val missingTileHandler = ExceptionHandler {
    case ex: TileNotFoundError => respondWithStatus(404) {
      complete(s"No tile: ${ex.getMessage}")
    }
  }

  def tms = handleExceptions(missingTileHandler) {
    pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layer, zoom, x, y) =>
      val key = SpatialKey(x, y)

      val extent = getMetaData(LayerId(layer, zoom)).mapTransform(key)
      val tile = tileReader(LayerId(layer, zoom)).read(key)

      {
        import DefaultJsonProtocol._
        pathPrefix("grid") {
          complete(tile.asciiDrawDouble())
        } ~
        pathPrefix("type") {
          complete(tile.getClass.getName)
        } ~
        pathPrefix("breaks") {
          complete(JsObject("classBreaks" -> tile.classBreaksDouble(100).toJson))
        } ~
        pathPrefix("histo") {
          val histo = tile.histogram
          val histoD = tile.histogramDouble
          val formattedHisto = {
            val buff = scala.collection.mutable.Buffer.empty[(Int, Long)]
            histo.foreach((v, c) => buff += ((v, c)))
            buff.to[Vector]
          }
          val formattedHistoD = {
            val buff = scala.collection.mutable.Buffer.empty[(Double, Long)]
            histoD.foreach((v, c) => buff += ((v, c)))
            buff.to[Vector]
          }
          complete(
            JsObject("histo" -> formattedHisto.toJson,
              "histoDouble" -> formattedHistoD.toJson)
          )
        } ~
        pathPrefix("stats") {
          complete {
            JsObject(
              "statistics" -> tile.statistics.toString.toJson,
              "statisticsDouble" -> tile.statisticsDouble.toString.toJson)
          }
        }
      } ~
      pathEnd {
        parameters(
          'breaks,
          'nodataColor ?,
          'colorRamp ? "blue-to-red"
        ) { (breaksParam, nodataColor, colorRamp) =>

          import geotrellis.raster._

          val ramp = ColorRampMap.getOrElse(colorRamp, ColorRamps.BlueToRed)
          val Color = """0x(\p{XDigit}{8})""".r
          val colorOptions = nodataColor match {
            case Some(Color(c)) =>
              ColorMap.Options.DEFAULT.copy(noDataColor = java.lang.Long.parseLong(c, 16).toInt)
            case _ => ColorMap.Options.DEFAULT
          }
          val colorMap =  {
            val breaks = breaksParam.split(",").map(_.toDouble)
            ramp.toColorMap(breaks, colorOptions)
          }

          respondWithMediaType(MediaTypes.`image/png`) {
            val result = tile.renderPng(colorMap).bytes
            complete(result)
          }
        }
      }
    }
  }
}
