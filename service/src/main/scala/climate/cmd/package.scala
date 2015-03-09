package climate

import geotrellis.spark._
import geotrellis.spark.ingest.NetCDFIngestCommand._
import geotrellis.spark.tiling._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.ingest.{Ingest, IngestArgs, HadoopIngestArgs, Tiler, Pyramid}
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.hadoop.formats.NetCdfBand
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark._
import com.quantifind.sumac.ArgMain
import geotrellis.spark.cmd.args._

package object cmd {
    implicit val tiler: Tiler[NetCdfBand, SpaceTimeKey] = {
      val getExtent = (inKey: NetCdfBand) => inKey.extent
      val createKey = (inKey: NetCdfBand, spatialComponent: SpatialKey) =>
        SpaceTimeKey(spatialComponent, inKey.time)

      Tiler(getExtent, createKey)
    }
}