package geotrellis.admin.translate

import java.io.File

import com.typesafe.config.ConfigFactory
import geotrellis.proj4._
import geotrellis.raster.Raster
import geotrellis.raster.io.Filesystem
import geotrellis.raster.io.arg.ArgReader
import geotrellis.raster.io.geotiff.SingleBandGeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter

class ARGToGeoTiffArgs extends TranslateArgs {

  /** CRS argument, if not specified it will check this metadata in the json file. */
  var CRS: Option[String] = None

  ignoredExtensions = Seq("arg")

}

/** Translate ARG format to GeoTiff format according to its json metadata. */
object ARGToGeoTiff extends TranslateCommand[ARGToGeoTiffArgs] {

  val help =
    s"""
       | Convert from Azavea Raster Grid format to single band GeoTiff LatLong.
       | Files keep the same name but change their extensions.
       | The CRS will be checked in metadata (EPSG code) if not given.
       | File must the json file containg the ARG metadatas.
       |
       | Usage: ARGToGeoTiff [--CRS 3785] --files file [file [...]]
       |
       | ARG documentation: http://geotrellis.io/documentation/0.9.0/geotrellis/io/arg/
       """.stripMargin

  def translate(file: File, args: ARGToGeoTiffArgs): Unit = {

    val path = file.getAbsolutePath
    require(path.endsWith("json"), "you must give the json metadata file")
    val argPath = changeExtension(path, "json", "tiff")

    val crs = CRS.fromName(args.CRS match {
      case Some(crsName) => crsName
      case None =>
        val json = ConfigFactory.parseString(Filesystem.readText(path))
        "EPSG:"+json.getString("epsg")
    })

    val Raster(tile, extent) = ArgReader.read(path).reproject(crs, LatLng)
    val geoTiff = SingleBandGeoTiff(tile, extent, LatLng)
    GeoTiffWriter.write(geoTiff, argPath)
  }

}
