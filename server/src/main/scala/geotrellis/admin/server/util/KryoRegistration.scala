package geotrellis.admin.server.util

import org.apache.spark.SparkConf
import org.apache.avro.Schema
import org.apache.avro.Schema.{Field, Type}

object KryoRegistration {
  /** Register any custom classes we need to serialize. */
  def register(conf: SparkConf): SparkConf = {
    val field = new Field("a", Schema.create(Type.NULL), null, null)
    val classes = classOf[org.apache.avro.Schema].getDeclaredClasses

    conf.registerKryoClasses(field.order.getClass +: classes)
  }
}
