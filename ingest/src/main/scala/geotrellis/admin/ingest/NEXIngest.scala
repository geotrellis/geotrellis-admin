package geotrellis.admin.ingest

import geotrellis.spark._
import geotrellis.spark.ingest._
import geotrellis.spark.tiling._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.index._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.utils.SparkUtils
import geotrellis.raster._

import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark._

import com.quantifind.sumac.ArgMain

/** Ingests the chunked NEX GeoTIFF data */
object NEXIngest extends ArgMain[AccumuloIngestArgs] with Logging {
  def main(args: AccumuloIngestArgs): Unit = {
    System.setProperty("com.sun.media.jai.disableMediaLib", "true")

    implicit val sparkContext = SparkUtils.createSparkContext("Ingest")
    val conf = sparkContext.hadoopConfiguration
    conf.set("io.map.index.interval", "1")

    sparkContext.setCheckpointDir("/Users/rob/proj/climate/")

    implicit val tiler: Tiler[SpaceTimeInputKey, SpaceTimeKey] = {
      val getExtent = (inKey: SpaceTimeInputKey) => inKey.extent
      val createKey = (inKey: SpaceTimeInputKey, spatialComponent: SpatialKey) =>
        SpaceTimeKey(spatialComponent, inKey.time)

      Tiler(getExtent, createKey)
    }

    implicit val accumulo = AccumuloInstance(args.instance, args.zookeeper, args.user, new PasswordToken(args.password))
    val layoutScheme = ZoomedLayoutScheme()

    def layerId(zoom: Int) = LayerId(args.layerName, zoom)
    val writer = AccumuloRasterCatalog().writer[SpaceTimeKey](ZCurveKeyIndexMethod.byYear, args.table)

    // Get source tiles
    val inPath = args.inPath
    val updatedConf =
      sparkContext.hadoopConfiguration.withInputDirectory(inPath)
    val source = 
      sparkContext.newAPIHadoopRDD(
        updatedConf,
        classOf[SourceTileInputFormat],
        classOf[SpaceTimeInputKey],
        classOf[Tile]
      )
    
    Ingest[SpaceTimeInputKey, SpaceTimeKey](source, args.destCrs, layoutScheme, args.pyramid){ (rdd, level) => 
              writer.write(LayerId(args.layerName, level.zoom), rdd)
    }
  }
}
