package geotrellis.admin.server.services

import io.finch._
import io.circe._
import io.finch.circe._

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.avro.Schema
import org.apache.avro.Schema.{Field, Type}
import com.twitter.finagle.{http, Http, Service}
import com.twitter.finagle.param.Stats
import com.twitter.server.TwitterServer
import com.twitter.finagle.http._
import com.twitter.util

import geotrellis.spark.io.s3._
import geotrellis.spark.io._
import geotrellis.spark._
import geotrellis.raster._
import geotrellis.raster.render._
import geotrellis.admin.shared._
import geotrellis.raster.histogram._

object Catalog {
  val conf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("GeoTrellis-Admin")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")

  implicit val sparkContext = new SparkContext(conf)
  val s3Bucket = "azavea-datahub"
  val s3Key = "catalog"

  val layerReader: LayerReader[LayerId] = S3LayerReader(s3Bucket, s3Key)
  val tileReader: S3ValueReader = S3ValueReader(s3Bucket, s3Key)
  val attributeStore = new S3AttributeStore(s3Bucket, s3Key)


  case class LayerMetadata(cellType: String, extent: String, crs: String, bounds: String)
  def describeContents(allZooms: Boolean) = {
    val layers = attributeStore.layerIds.filter(_.zoom != 0 || allZooms)
    val names = layers.map(_.name).distinct

    names.map { lName: String =>
      LayerDescription(lName, layers.filter(_.name == lName).map(_.zoom).sorted)
    }
  }

  def layerBreaks(layerId: LayerId, numBreaks: Int) = {
    val data = layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](layerId)
    val doubleBreaks = data.classBreaksDouble(numBreaks)
    ClassBreaks(doubleBreaks)
  }

  def getTile(layerId: LayerId, x: Int, y: Int): Tile = {
    val key = SpatialKey(x, y)
    val metadata = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)
    val extent = metadata.mapTransform(key)
    val keybounds = metadata.bounds
    val tile = tileReader.reader[SpatialKey, Tile](layerId).read(key)
    tile
  }

  def getRDDHistogram(layerId: LayerId): Histogram[Double] = {
    layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](layerId).histogram()
  }

  val listRoute = get("catalog" :: paramOption("allZooms").as[Boolean]) { allZooms: Option[Boolean] =>
    Ok(describeContents(allZooms.getOrElse(false)))
  }

  val metadataRoute = get("catalog" :: string("layerName") :: int("zoom") :: "metadata") {
    (layerName: String, zoom: Int) => Ok {
      val layerId = LayerId(layerName, zoom)
      val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)
      LayerMetadata(md.cellType.toString, md.extent.toString, md.crs.toProj4String, md.bounds.toString)
    }
  }

  val breaksRoute = get("catalog" :: string("layerName") :: int("zoom") :: paramOption("breaks").as[Int]) {
    (layerName: String, zoom: Int, numBreaks: Option[Int]) =>
      Ok(layerBreaks(LayerId(layerName, zoom), numBreaks.getOrElse(5)))
  }

  implicit val encodeException: Encoder[TileNotFoundError] = Encoder.instance(e =>
    Json.obj("message" -> Json.fromString(e.getMessage)))

  val tileRoute = get("catalog" :: string("layerName") :: int("zoom") :: int("x") :: int("y") :: paramOption("tileFormat").as[String]) {
    (layerName: String, zoom: Int, x: Int, y: Int, tileFormat: Option[String]) =>
      tileFormat match {
        case Some("ascii") =>
          Ok {
            val layerId = LayerId(layerName, zoom)
            val tile = getTile(layerId, x, y)
            tile.asciiDraw()
          }
        case _ =>
          Ok {
            val layerId = LayerId(layerName, zoom)
            val tile = getTile(layerId, x, y)
            val histogram = getRDDHistogram(layerId)
            val colorMap = ColorMap.fromQuantileBreaks(histogram, ColorRamps.BlueToOrange)
            tile.renderPng(colorMap).bytes
          }.withHeader(("Content-Type", "image/png"))
      }
  }.handle { case tnf: TileNotFoundError => NotFound(tnf) }.toService
}
