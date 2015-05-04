package geotrellis.admin.ingest

import geotrellis.spark._
import geotrellis.spark.ingest._
import geotrellis.spark.io.hadoop._
import geotrellis.raster._
import geotrellis.raster.io.geotiff.reader._
import geotrellis.vector._
import geotrellis.proj4._

import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.FSDataInputStream

import org.apache.hadoop.mapreduce.InputSplit
import org.apache.hadoop.mapreduce.JobContext
import org.apache.hadoop.mapreduce.RecordReader
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.input.FileSplit

import java.nio.ByteBuffer

import com.github.nscala_time.time.Imports._

case class SpaceTimeInputKey(extent: Extent, crs: CRS, time: DateTime)
object SpaceTimeInputKey {
  implicit object IngestKey extends KeyComponent[SpaceTimeInputKey, ProjectedExtent] {
    def lens = createLens(
      key => ProjectedExtent(key.extent, key.crs),
      pe => key => SpaceTimeInputKey(pe.extent, pe.crs, key.time)
    )
  }
}

class SourceTileInputFormat extends FileInputFormat[SpaceTimeInputKey, Tile] {
  override def isSplitable(context: JobContext, fileName: Path) = false

  override def createRecordReader(
    split: InputSplit,
    context: TaskAttemptContext): RecordReader[SpaceTimeInputKey, Tile] = new InputTileRecordReader

}

class InputTileRecordReader extends RecordReader[SpaceTimeInputKey, Tile] {
  private var tup: (SpaceTimeInputKey, Tile) = null
  private var hasNext: Boolean = true

  def initialize(split: InputSplit, context: TaskAttemptContext) = {
    val path = split.asInstanceOf[FileSplit].getPath()
    val conf = context.getConfiguration()
    val bytes = HdfsUtils.readBytes(path, conf)

    val gt = GeoTiffReader.read(bytes)
    val meta = gt.tags
    val isoString = meta("ISO_TIME")
    val dateTime = DateTime.parse(isoString)
    val GeoTiffBand(tile, extent, crs, _) =
      gt.bands.head

    tup = (SpaceTimeInputKey(extent, crs, dateTime), tile)
  }

  def close = {}
  def getCurrentKey = tup._1
  def getCurrentValue = { hasNext = false ; tup._2 }
  def getProgress = 1
  def nextKeyValue = hasNext
}
