package geotrellis.admin.server

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import geotrellis.proj4._
import geotrellis.raster._
import geotrellis.raster.render._
import geotrellis.raster.resample._
import geotrellis.raster.histogram._
import geotrellis.raster.io.json._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.json._
import geotrellis.spark.tiling._
import geotrellis.vector._
import geotrellis.vector.reproject._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import geotrellis.raster.render._

object TMS {/*
  def retrieveTile(layer: String, zoom: Int, x: Int, y: Int, timeOption: Option[String], breaksOption: Option[String], colorOption: Option[String]): Array[Byte] = {
    val zooms = zoomLevelsFor(layer)
    val tile =
      if(zooms.contains(zoom)) {
        val layerId = LayerId(layer, zoom)

        timeOption match {
          case Some(timeStr) =>
            val time = DateTime.parse(timeStr)

            catalog.tileReader[SpaceTimeKey](layerId).read(SpaceTimeKey(x, y, time))
          case None =>
            catalog.tileReader[SpatialKey](layerId).read(SpatialKey(x, y))
        }
      } else {
        val z = zooms.max

        if(zoom > z) {
          val layerId = LayerId(layer, z)

          metaDatas.get(layerId) match {
            case Some(meta) =>
              val rmd = meta.rasterMetaData

              val layoutLevel = layoutScheme.levelFor(zoom)
              val mapTransform = MapKeyTransform(rmd.crs, layoutLevel.tileLayout.layoutCols, layoutLevel.tileLayout.layoutRows)
              val targetExtent = mapTransform(x, y)
              val gb @ GridBounds(nx, ny, _, _) = rmd.mapTransform(targetExtent)
              val sourceExtent = rmd.mapTransform(nx, ny)

              val largerTile =
                timeOption match {
                  case Some(timeStr) =>
                    val time = DateTime.parse(timeStr)
                    catalog.tileReader[SpaceTimeKey](layerId).read(SpaceTimeKey(nx, ny, time))
                  case None =>
                    catalog.tileReader[SpatialKey](layerId).read(SpatialKey(nx, ny))
                }

              largerTile.resample(sourceExtent, RasterExtent(targetExtent, 256, 256))
            case None =>
              sys.error(s"$layerId not found in catalog.")
          }
        } else {
          sys.error("No zoom level for this layer.")
        }
      }

    val colorRamp: ColorRamp =
      colorOption.flatMap(Colors.apply).getOrElse(ColorRamps.HeatmapBlueToYellowToRedSpectrum)

    breaksOption match {
      case Some(breaks) =>
        tile.renderPng(colorRamp, breaks.split(",").map(_.toInt)).bytes
      case None =>
        tile.renderPng.bytes
    }
  }

  def route =
    get(string :: int :: int :: int :: paramOption("time") :: paramOption("breaks") :: paramOption("colorRamp")) {
      (layer: String, zoom: Int, x: Int, y: Int, timeOption: Option[String], breaksOption: Option[String], colorOption: Option[String]) =>
        Ok { retrieveTile(layer, zoom, x, y, timeOption, breaksOption, colorOption) }.withHeader("Content-Type" -> "image/png")
    }*/
}
