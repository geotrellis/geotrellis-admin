package geotrellis.admin.server.util

import org.apache.spark.SparkConf
import org.apache.avro.Schema
import org.apache.avro.Schema.{Field, Type}

object AvroRegistrator {
  def apply(conf: SparkConf): SparkConf =
    conf.registerKryoClasses(
      new Field("a", Schema.create(Type.NULL), null, null).order.getClass +: classOf[org.apache.avro.Schema].getDeclaredClasses
    )
}
