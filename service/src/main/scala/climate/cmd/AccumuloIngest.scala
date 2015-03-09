package climate.cmd

import geotrellis.spark._
import geotrellis.spark.tiling._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.ingest.{Ingest, Pyramid, AccumuloIngestArgs}
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.hadoop.formats._
import geotrellis.spark.utils.SparkUtils

import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark._
import com.quantifind.sumac.ArgMain

object AccumuloIngestCommand extends ArgMain[AccumuloIngestArgs] with Logging {
  def main(args: AccumuloIngestArgs): Unit = {
    System.setProperty("com.sun.media.jai.disableMediaLib", "true")

    implicit val sparkContext = SparkUtils.createSparkContext("Ingest")

    val accumulo = AccumuloInstance(args.instance, args.zookeeper, args.user, new PasswordToken(args.password))
    val source = sparkContext.netCdfRDD(args.inPath).repartition(args.partitions)

    val layoutScheme = ZoomedLayoutScheme(64)
    val (level, rdd) =  Ingest[NetCdfBand, SpaceTimeKey](source, args.destCrs, layoutScheme)

    val save = { (rdd: RasterRDD[SpaceTimeKey], level: LayoutLevel) =>
      accumulo.catalog.save(LayerId(args.layerName, level.zoom), args.table, rdd, args.clobber)
    }

    if (args.pyramid) {
      Pyramid.saveLevels(rdd, level, layoutScheme)(save)
    } else{
      save(rdd, level)
    }
  }
}
