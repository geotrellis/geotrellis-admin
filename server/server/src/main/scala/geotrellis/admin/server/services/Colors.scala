package geotrellis.admin.server.services

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

import geotrellis.raster.render._

case class ColorMapping(name: String, url: String)

object Colors {

  def apply(s:String): Option[ColorRamp] = rampMap.get(s)

  def getOrElse(s:String, cr:ColorRamp): ColorRamp = rampMap.getOrElse(s,cr)

  val rampMap: Map[String, ColorRamp] =
    Map(
      "blue-to-orange" -> ColorRamps.BlueToOrange,
      "green-to-orange" -> ColorRamps.LightYellowToOrange,
      "blue-to-red" -> ColorRamps.BlueToRed,
      "green-to-red-orange" -> ColorRamps.GreenToRedOrange,
      "light-to-dark-sunset" -> ColorRamps.LightToDarkSunset,
      "light-to-dark-green" -> ColorRamps.LightToDarkGreen,
      "yellow-to-red-heatmap" -> ColorRamps.HeatmapYellowToRed,
      "blue-to-yellow-to-red-heatmap" -> ColorRamps.HeatmapBlueToYellowToRedSpectrum,
      "dark-red-to-yellow-heatmap" -> ColorRamps.HeatmapDarkRedToYellowWhite,
      "purple-to-dark-purple-to-white-heatmap" -> ColorRamps.HeatmapLightPurpleToDarkPurpleToWhite,
      "bold-land-use-qualitative" -> ColorRamps.ClassificationBoldLandUse,
      "muted-terrain-qualitative" -> ColorRamps.ClassificationMutedTerrain
    )

  def route = get("colors") {
    val colorMappings = rampMap.keys.map { key => ColorMapping(key, s"img/ramps/${key}.png") }
    Ok(colorMappings)
  }
}
