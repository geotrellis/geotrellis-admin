package geotrellis.admin.ingest

import geotrellis.raster.io.json._
import geotrellis.spark._
import geotrellis.spark.ingest._
import geotrellis.spark.cmd.args.AccumuloArgs
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.index._
import geotrellis.spark.op.stats._
import geotrellis.spark.tiling._
import geotrellis.spark.utils.SparkUtils
import geotrellis.vector.ProjectedExtent

import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark._
import com.quantifind.sumac.ArgMain
import com.quantifind.sumac.validation.Required

class AccumuloPyramidArgs extends IngestArgs with AccumuloArgs {
  @Required var table: String = _
  @Required var startLevel: Int = _
}

object AccumuloPyramidCommand extends ArgMain[AccumuloPyramidArgs] with Logging {
  def main(args: AccumuloPyramidArgs): Unit = {
    System.setProperty("com.sun.media.jai.disableMediaLib", "true")

    implicit val sparkContext = SparkUtils.createSparkContext("Ingest")

    implicit val accumulo = AccumuloInstance(args.instance, args.zookeeper, args.user, new PasswordToken(args.password))

    val source = sparkContext.hadoopGeoTiffRDD(args.inPath).repartition(args.partitions)
    val layoutScheme = ZoomedLayoutScheme(256)

    val catalog = AccumuloRasterCatalog()
    val writer = catalog.writer[SpatialKey](RowMajorKeyIndexMethod, args.table)
    val attributeStore = catalog.attributeStore

    Ingest[ProjectedExtent, SpatialKey](source, args.destCrs, layoutScheme, pyramid = true) {
      (rdd: RasterRDD[SpatialKey], level: LayoutLevel) =>
        val layerId = LayerId(args.layerName, level.zoom)
        writer.write(layerId, rdd)
        attributeStore.write(layerId, "histogram", rdd.histogram)
    }
  }
}
