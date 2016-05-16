package geotrellis.admin.server.util

import org.apache.spark.SparkConf
import org.apache.avro.Schema

object KryoRegistration {
  /** Register any custom classes we need to serialize. */
  def register(conf: SparkConf): SparkConf = {
    /* `Schema.Field` has one further subclass, and it is not included upon
     * a `getDeclaredClasses` call on `Schema`.
     */
    val classes = classOf[Schema].getDeclaredClasses

    conf.registerKryoClasses(classOf[Schema.Field.Order] +: classes)
  }
}
