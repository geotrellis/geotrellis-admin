package geotrellis.admin.server.services

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import io.circe._

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


  case class LayerDescription(name: String, availableZooms: Seq[Int])
  case class LayerMetadata(cellType: String, extent: String, crs: String, bounds: String)
  def describeContents(allZooms: Boolean) = {
    val layers = attributeStore.layerIds.filter(_.zoom != 0 || allZooms)
    val names = layers.map(_.name).distinct

    names.map { lName: String =>
      LayerDescription(lName, layers.filter(_.name == lName).map(_.zoom).sorted)
    }
  }

  def layerBreaks(layerId: LayerId, numBreaks: Option[Int]) = {
    val data = layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](layerId)
    val breaks = data.classBreaks(numBreaks.getOrElse(4)).map(_.toDouble)
    val doubleBreaks = data.classBreaksDouble(numBreaks.getOrElse(4))
    Map(("classBreaks" -> breaks), ("classBreaksDouble", doubleBreaks))
  }

  def getTile(layerId: LayerId, x: Int, y: Int): Tile = {
    val key = SpatialKey(x, y)
    val metadata = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)
    val extent = metadata.mapTransform(key)
    val keybounds = metadata.bounds
    println(keybounds)
    tileReader.reader[SpatialKey, Tile](layerId).read(key)
  }

  val listRoute = get("catalog" :: paramOption("allZooms").as[Boolean]) { allZooms: Option[Boolean] =>
    Ok(describeContents(allZooms.getOrElse(false)))
  }

  val metadataRoute = get("catalog" :: string("layerName")) {
    (layerName: String) => Ok {
      val layerId = attributeStore.layerIds.filter(_.zoom != 0).filter(_.name == layerName).head
      val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)
      LayerMetadata(md.cellType.toString, md.extent.toString, md.crs.toProj4String, md.bounds.toString)
    }
  }

  val breaksRoute = get("catalog" :: string("layerName") :: int("zoom") :: paramOption("numBreaks").as[Int]) {
    (layerName: String, zoom: Int, numBreaks: Option[Int]) =>
      Ok(layerBreaks(LayerId(layerName, zoom), numBreaks))
  }

  implicit val encodeException: Encoder[TileNotFoundError] = Encoder.instance(e =>
    Json.obj("message" -> Json.fromString(e.getMessage)))

  val tileRoute = get("catalog" :: string("layerName") :: int("zoom") :: int("x") :: int("y") :: paramOption("tileFormat").as[String]) {
    (layerName: String, zoom: Int, x: Int, y: Int, tileFormat: Option[String]) =>
      Ok(getTile(LayerId(layerName, zoom), x, y).renderPng(ColorRamps.BlueToOrange))
  }.withHeader("Content-Type" -> "image/png") handle {
    case tnf: TileNotFoundError => NotFound(tnf)
  }
}
