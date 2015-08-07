package geotrellis.admin.translate

import java.io.File

import com.typesafe.config.ConfigFactory
import geotrellis.proj4._
import geotrellis.raster.Raster
import geotrellis.raster.io.Filesystem
import geotrellis.raster.io.arg.ArgReader
import geotrellis.raster.io.geotiff.SingleBandGeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter

case class ARGToGeoTiffConfig(var crs: Option[String] = None) extends TranslateConfig(ignoredExtensions = Seq("arg"))

/** Translate ARG format to GeoTiff format according to its json metadata. */
object ARGToGeoTiff extends TranslateCommand[ARGToGeoTiffConfig] {

  val name = "ARGToGeoTiff"
  val config = ARGToGeoTiffConfig()

  val description =
    s"""Convert from Azavea Raster Grid format to single band GeoTiff LatLong.
       |Files keep the same name but change their extensions.
       |The CRS will be checked in metadata (EPSG code) if not given.
       |ARG documentation: http://geotrellis.io/documentation/0.9.0/geotrellis/io/arg/
     """.stripMargin

  parser.opt[String]("CRS") foreach { x =>
    config.crs = Some(x)
  } text "specify CRS, if not given it will check this in the json file"

  def translate(file: File, args: ARGToGeoTiffConfig): Unit = {

    val path = file.getAbsolutePath
    if (args.verbose) println(s"Translating $path")
    require(path.endsWith("json"), "you must give the json metadata file")
    val argPath = changeExtension(path, "json", "tiff")

    val json = ConfigFactory.parseString(Filesystem.readText(path))
    val crs = CRS.fromName(args.crs match {
      case Some(crsName) => crsName
      case None => "EPSG:" + json.getString("epsg")
    })

    val Raster(tile, extent) = ArgReader.read(path).reproject(crs, LatLng)
    val geoTiff = SingleBandGeoTiff(tile, extent, LatLng)
    GeoTiffWriter.write(geoTiff, argPath)
  }

}
