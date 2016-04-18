package geotrellis.admin.client.circuit

import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global

object Catalog {

  def list = Ajax.get("http://localhost:8088/catalog")

}
