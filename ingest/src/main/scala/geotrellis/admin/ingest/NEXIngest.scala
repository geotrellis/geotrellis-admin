package geotrellis.admin.ingest

import geotrellis.spark._
import geotrellis.spark.ingest._
import geotrellis.spark.cmd.args.AccumuloArgs
import geotrellis.spark.ingest.NetCDFIngestCommand._
import geotrellis.spark.tiling._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.index._
import geotrellis.spark.cmd.args._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.utils.SparkUtils
import geotrellis.raster._
import geotrellis.vector._
import geotrellis.proj4._
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark._
import com.quantifind.sumac.ArgMain
import com.github.nscala_time.time.Imports._
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
/*
    val save = { (rdd: RasterRDD[SpaceTimeKey], level: LayoutLevel) =>
      accumulo.catalog.save(layerId(level.zoom), args.table, rdd, args.clobber)
    }
*/
    //Sage wrote this part
    val writer = AccumuloRasterCatalog().writer[SpaceTimeKey](HilbertKeyIndexMethod.apply(5), args.table)
/*

    Ingest[ProjectedExtent, SpaceTimeKey](source, args.destCrs, layoutScheme, args.pyramid){ (rdd, level) => 
      writer.write(LayerId(args.layerName, level.zoom), rdd)
    }
*/
    //end
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
/*
    Ingest[SpaceTimeInputKey, SpaceTimeKey](source, args.destCrs, layoutScheme, args.pyramid){ (rdd, level) => 
              accumulo.catalog.save(LayerId(args.layerName, level.zoom), args.table, rdd, args.clobber)
    }
*/
    
    Ingest[SpaceTimeInputKey, SpaceTimeKey](source, args.destCrs, layoutScheme, args.pyramid){ (rdd, level) => 
              writer.write(LayerId(args.layerName, level.zoom), rdd)
    }
  }
}
