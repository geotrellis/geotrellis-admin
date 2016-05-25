package geotrellis.admin.server

import scala.concurrent.Future

import akka.actor._
import geotrellis.admin.server.util._
import geotrellis.raster._
import geotrellis.raster.render._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.vector.io.json.Implicits._
import org.apache.spark.{SparkConf, SparkContext}
import scala.util.Try
import spray.caching._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

class AdminActor extends Actor with AdminRoutes {
  val conf: SparkConf = KryoRegistration.register(
    new SparkConf()
      .setMaster(sys.env("SPARK_MASTER"))
      .setAppName("geotrellis-admin")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")
      .setJars(SparkContext.jarOfObject(this).toList)
  )

  implicit val sparkContext: SparkContext = new SparkContext(conf)

  override def actorRefFactory: ActorContext = context
  override def receive = runRoute(serviceRoute)

  val s3Bucket: String = sys.env("S3_BUCKET")
  val s3Key: String = sys.env("S3_KEY")

  override lazy val reader: LayerReader[LayerId] = S3LayerReader(s3Bucket, s3Key)
  override lazy val attributeStore: AttributeStore = S3AttributeStore(s3Bucket, s3Key)
  override def tileReader(id: LayerId): Reader[SpatialKey, Tile] =
    S3ValueReader(attributeStore, id)

}

trait AdminRoutes extends HttpService with CORSSupport {
  implicit val sparkContext: SparkContext

  implicit val executionContext = actorRefFactory.dispatcher

  def reader: LayerReader[LayerId]
  def attributeStore: AttributeStore
  def tileReader(id: LayerId): Reader[SpatialKey, Tile]

  val baseZoomLevel: Int = 4

  val breaksStore: Cache[Array[Double]] = LruCache()
  val metadataStore: Cache[TileLayerMetadata[SpatialKey]] = LruCache()

  /* Extra attributes besides metadata */
  val extraAttrStore: Cache[Map[String, JsValue]] = LruCache()

  /* Uncoloured Tiles
   * While Leaflet does cache coloured tiles to a degree,
   * this cache is useful if the user changes colour ramps.
   */
  val tileStore: Cache[Tile] = LruCache(initialCapacity = 64)

  def layerId(layer: String): LayerId =
    LayerId(layer, baseZoomLevel)

  // TODO think about the performance of using a string for the key
  def getMetadata(id: LayerId): Future[TileLayerMetadata[SpatialKey]] = metadataStore(s"${id.name}/${id.zoom}") {
    attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](id)
  }

  /* Find extra attributes written alongside normal metadata */
  def extraAttributes(id: LayerId): Seq[String] =
    attributeStore.availableAttributes(id)

  /* Fetch a tile, either from the cache or S3 */
  def getTile(id: LayerId, key: SpatialKey): Future[Tile] =
    tileStore(s"${id.name}/${id.zoom}/${key.col}/${key.row}") {
      tileReader(id).read(key)
    }

  def serviceRoute =
    cors {
      get {
        pathPrefix("gt") {
          pathPrefix("errorTile")(errorTile) ~
            pathPrefix("bounds")(bounds) ~
            pathPrefix("metadata")(metadata) ~
            pathPrefix("layers")(layers) ~
            pathPrefix("attributes")(attributes) ~
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

  def errorTile = respondWithMediaType(MediaTypes.`image/png`) {
    complete(ErrorTile.bytes)
  }

  /** Get the grid bounds for a Layer at a given zoom level */
  def bounds = pathPrefix(Segment / IntNumber) { (layerName, zoom) =>
    import EndpointProtocol._

    complete {
      getMetadata(LayerId(layerName, zoom)).map(_.gridBounds)
    }
  }

  /** Fetch all Layer names and their available zoom levels */
  def layers = {
    import EndpointProtocol._
    complete {
      attributeStore.layerIds
        .groupBy(_.name)
        .map({ case (k, v) => (k, v.map(_.zoom).sorted) })
        .map(l => LayerDescription(l._1, l._2))
    }
  }

  /** Get extra non-meta attributes from a Layer */
  def attributes = pathPrefix(Segment / IntNumber) { (layerName, zoom) =>
    import DefaultJsonProtocol._

    complete {
      val id = LayerId(layerName, zoom)

      /* Cache the results of attribute calls */
      extraAttrStore(s"${id.name}/${id.zoom}") {
        extraAttributes(id).foldRight(Map.empty[String, JsValue]) {
          case ("metadata", acc) => acc
          case (att, acc) => acc + (att -> attributeStore.read[JsValue](id, att))
        }
      }
    }
  }

  /** Get a Layer's metadata for some zoom level */
  def metadata = pathPrefix(Segment / IntNumber) { (layerName, zoom) =>
    import DefaultJsonProtocol._

    complete {
      val id = LayerId(layerName, zoom)

      /* Two things can go wrong here:
       *   1. The metadata can be invalid, and fail to parse. In this case,
       *      we try to serve the raw JSON of the metadata.
       *   2. There is no metadata for this layer+zoom.
       *
       * Exceptions are thrown in either case, so we must guard.
       */
      getMetadata(id).map(_.toJson.asJsObject).recover({
        case e: Throwable => {
          Try(attributeStore.read[JsObject](id, "metadata"))
            .getOrElse(JsObject("error" -> JsString("No metadata for this layer/zoom.")))
        }
      })
    }
  }

  /**
   * Calculate and store class breaks for a layer, and yield a UUID that
   * references the saved breaks. Necessary for a `/tms/...` call.
   */
  def breaks = pathPrefix(Segment / IntNumber) { (layer, numBreaks) =>
    import DefaultJsonProtocol._
    complete {
      val data = reader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](layerId(layer))

      val breaks: Array[Double] = data.classBreaksDouble(numBreaks)
      val uuid: String = java.util.UUID.randomUUID.toString
      breaksStore(uuid)(breaks)

      JsObject("classBreaks" -> JsString(uuid))
    }
  }

  val missingTileHandler = ExceptionHandler {
    case ex: TileNotFoundError => respondWithStatus(404) {
      complete(s"No tile: ${ex.getMessage}")
    }
  }

  /** Fetch information about a Tile, or the Tile itself as a PNG */
  def tms = handleExceptions(missingTileHandler) {
    pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layer, zoom, x, y) =>
      val key = SpatialKey(x, y)
      val layerId = LayerId(layer, zoom)

      pathPrefix("grid")(tmsGrid(layerId, key)) ~
        pathPrefix("type")(tmsType(layerId, key)) ~
        pathPrefix("breaks")(tmsBreaks(layerId, key)) ~
        pathPrefix("histo")(tmsHisto(layerId, key)) ~
        pathPrefix("stats")(tmsStats(layerId, key)) ~
        pathEnd(serveTile(layerId, key))
    }
  }

  def tmsGrid(layerId: LayerId, key: SpatialKey): StandardRoute = {
    complete {
      getTile(layerId, key).map(_.asciiDrawDouble())
    }
  }

  def tmsType(layerId: LayerId, key: SpatialKey): StandardRoute = {
    complete {
      getTile(layerId, key).map(_.getClass.getName)
    }
  }

  def tmsBreaks(layerId: LayerId, key: SpatialKey): StandardRoute = {
    import DefaultJsonProtocol._

    complete {
      getTile(layerId, key).map(t =>
        JsObject("classBreaks" -> t.classBreaksDouble(100).toJson)
      )
    }
  }

  def tmsHisto(layerId: LayerId, key: SpatialKey): StandardRoute = {
    import DefaultJsonProtocol._

    complete {
      getTile(layerId, key).map({ tile =>
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

        JsObject(
          "histo" -> formattedHisto.toJson,
          "histoDouble" -> formattedHistoD.toJson
        )
      })
    }
  }

  def tmsStats(layerId: LayerId, key: SpatialKey): StandardRoute = {
    import DefaultJsonProtocol._

    complete {
      getTile(layerId, key).map({ tile =>
        JsObject(
          "statistics" -> tile.statistics.toString.toJson,
          "statisticsDouble" -> tile.statisticsDouble.toString.toJson
        )
      })
    }
  }

  def serveTile(layerId: LayerId, key: SpatialKey) = {
    parameters(
      'breaks,
      'opacity ? 100.0,
      'nodataColor ?,
      'colorRamp ? "blue-to-red"
    ) { (breaksParam, opacityParam, nodataColor, colorRamp) =>
        import geotrellis.raster._

        respondWithMediaType(MediaTypes.`image/png`) {
          complete {
            getMetadata(layerId).flatMap { md: TileLayerMetadata[SpatialKey] =>
              if (!md.bounds.includes(key)) {
                /* There is no tile data at this (x,y) location,
                 * so don't bother calling the remote store (S3) for it.
                 */
                Future.successful(None)
              } else {
                /* Prepare to yield a coloured Tile */
                getTile(layerId, key).flatMap({ tile =>
                  val ramp: ColorRamp = ColorRampMap.getOrElse(colorRamp, ColorRamps.BlueToRed)
                  val color = """0x(\p{XDigit}{8})""".r
                  val colorOptions: ColorMap.Options = nodataColor match {
                    case Some(color(c)) =>
                      ColorMap.Options.DEFAULT
                        .copy(noDataColor = java.lang.Long.parseLong(c, 16).toInt)
                    case _ => ColorMap.Options.DEFAULT
                  }

                  breaksStore.get(breaksParam) match {
                    case None => Future.successful(None)
                    case Some(fut) => {
                      fut.map { b: Array[Double] =>
                        val colorMap = ramp.setAlpha(opacityParam)
                          .toColorMap(b, colorOptions)
                        Some(tile.renderPng(colorMap).bytes)
                      }
                    }
                  }
                })
              }
            }
          }
        }
      }
  }
}
