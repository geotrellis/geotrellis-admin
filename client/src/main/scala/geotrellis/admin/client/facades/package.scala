package geotrellis.admin.client

package object facades {
  /**
  * A map of option values, which JSOptionBuilder builds up.
  */
  type OptMap = Map[String, Any]
  val noOpts = Map.empty[String, Any]
}
