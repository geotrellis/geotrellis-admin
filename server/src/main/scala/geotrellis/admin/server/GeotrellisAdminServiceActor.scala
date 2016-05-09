package geotrellis.admin.server

import akka.actor._
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark.{SparkConf, SparkContext}
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._
import spray.caching._

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


class GeotrellisAdminServiceActor extends Actor with GeotrellisAdminService{
  val conf = AvroRegistrator(
    new SparkConf()
      .setMaster(sys.env("SPARK_MASTER"))
      .setAppName("geotrellis-admin")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")
      .setJars(SparkContext.jarOfObject(this).toList)
  )

  implicit val sparkContext = new SparkContext(conf)

  override def actorRefFactory = context
  override def receive = runRoute(serviceRoute)

  val s3Bucket = sys.env("S3_BUCKET")
  val s3Key = sys.env("S3_KEY")

  override lazy val reader: LayerReader[LayerId] = S3LayerReader(s3Bucket, s3Key)
  override lazy val attributeStore: AttributeStore = S3AttributeStore(s3Bucket, s3Key)
  override def tileReader(id: LayerId): Reader[SpatialKey, Tile] =
    S3ValueReader(attributeStore, id)

}

trait GeotrellisAdminService extends HttpService with CORSSupport {
  implicit val sparkContext: SparkContext

  implicit val executionContext = actorRefFactory.dispatcher

  def reader: LayerReader[LayerId]
  def attributeStore: AttributeStore
  def tileReader(id: LayerId): Reader[SpatialKey, Tile]

  val baseZoomLevel = 4

  val breaksStore: Cache[Array[Double]] = LruCache()

  def layerId(layer: String): LayerId =
    LayerId(layer, baseZoomLevel)

  def getMetaData(id: LayerId): TileLayerMetadata[SpatialKey] =
    attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](id)

  def serviceRoute =
    cors {
      get {
        pathPrefix("gt") {
          pathPrefix("errorTile")(errorTile) ~
          pathPrefix("bounds")(bounds) ~
          pathPrefix("metadata")(metadata) ~
          pathPrefix("layers")(layers) ~
          pathPrefix("tms")(tms) ~
          pathPrefix("breaks")(breaks)
        }
      }
    }

  case class LayerDescription(name: String, availableZooms: Seq[Int])
  object EndpointProtocol extends DefaultJsonProtocol {
    implicit val ldFormat = jsonFormat2(LayerDescription)
    implicit val gbFormat = jsonFormat4(GridBounds.apply)
  }

  def errorTile =
    respondWithMediaType(MediaTypes.`image/png`) {
      complete(util.ErrorTile())
    }

  def bounds = pathPrefix(Segment / IntNumber) { (layerName, zoom) =>
    import EndpointProtocol._
    complete {
      val data = reader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](LayerId(layerName, zoom))
      val bounds = data.metadata.gridBounds
      bounds
    }
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
      val metadata = getMetaData(layer)
      metadata
    }
  }

  def breaks = pathPrefix(Segment / IntNumber) { (layer, numBreaks) =>
    import DefaultJsonProtocol._
    complete {
      val data = reader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](layerId(layer))

      val breaks = data.classBreaksDouble(numBreaks)
      val uuid = java.util.UUID.randomUUID.toString
      breaksStore(uuid)(breaks)

      JsObject("classBreaks" -> JsString(uuid))
    }
  }

  val missingTileHandler = ExceptionHandler {
    case ex: TileNotFoundError => respondWithStatus(404) {
      complete(s"No tile: ${ex.getMessage}")
    }
  }

  def tms = handleExceptions(missingTileHandler) {
    pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layer, zoom, x, y) =>
      val key = SpatialKey(x, y)

      {
        import DefaultJsonProtocol._
        pathPrefix("grid") {
          complete {
            val tile = tileReader(LayerId(layer, zoom)).read(key)
            tile.asciiDrawDouble()
          }
        } ~
        pathPrefix("type") {
          complete {
            val tile = tileReader(LayerId(layer, zoom)).read(key)
            tile.getClass.getName
          }
        } ~
        pathPrefix("breaks") {
          complete {
            val tile = tileReader(LayerId(layer, zoom)).read(key)
            JsObject("classBreaks" -> tile.classBreaksDouble(100).toJson)
          }
        } ~
        pathPrefix("histo") {
          complete {
            val tile = tileReader(LayerId(layer, zoom)).read(key)
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
              JsObject("histo" -> formattedHisto.toJson,
                "histoDouble" -> formattedHistoD.toJson)
          }
        } ~
        pathPrefix("stats") {
          complete {
            val tile = tileReader(LayerId(layer, zoom)).read(key)
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
          respondWithMediaType(MediaTypes.`image/png`) {
            complete {
              val tile = tileReader(LayerId(layer, zoom)).read(key)

              val ramp = ColorRampMap.getOrElse(colorRamp, ColorRamps.BlueToRed)
              val Color = """0x(\p{XDigit}{8})""".r
              val colorOptions = nodataColor match {
                case Some(Color(c)) =>
                  ColorMap.Options.DEFAULT.copy(noDataColor = java.lang.Long.parseLong(c, 16).toInt)
                case _ => ColorMap.Options.DEFAULT
              }
              breaksStore.get(breaksParam).get.map { b: Array[Double] =>
                val colorMap =  {
                  ramp.toColorMap(b, colorOptions)
                }
                tile.renderPng(colorMap).bytes
              }
            }
          }
        }
      }
    }
  }
}
