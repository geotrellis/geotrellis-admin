package geotrellis.admin.ingest

import com.quantifind.sumac.ArgMain
import com.quantifind.sumac.validation.Required
import geotrellis.spark._
import geotrellis.spark.cmd.args._
import geotrellis.spark.ingest.IngestArgs
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.op.stats._
import geotrellis.spark.utils.SparkUtils
import geotrellis.vector.Extent
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.hadoop.fs.Path
import org.apache.spark._

class CalculateArgs extends AccumuloArgs {
  @Required var inputLayer: String = _
  @Required var outputLayer: String = _
}

/**
 * Ingests raw multi-band NetCDF tiles into a re-projected and tiled RasterRDD
 */
object Calculate extends ArgMain[CalculateArgs] with Logging {
  def main(args: CalculateArgs): Unit = {
    implicit val sparkContext = SparkUtils.createSparkContext("Calculate")

    // val accumulo = AccumuloInstance(args.instance, args.zookeeper, args.user, new PasswordToken(args.password))
    // val catalog = accumulo.catalog
    val catalog: HadoopCatalog = HadoopCatalog(sparkContext, new Path("hdfs://localhost/catalog"))

    val rdd = catalog.load[SpaceTimeKey](LayerId(args.inputLayer, 2))
    
    val ret = rdd
      .mapKeys { key => key.updateTemporalComponent(key.temporalKey.time.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0)) }
      .averageByKey
    catalog.save[SpaceTimeKey](LayerId(args.outputLayer,2), "results", ret, true)
  }
}
