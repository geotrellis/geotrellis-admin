package geotrellis.admin.client

import geotrellis.admin.client.components._
import geotrellis.admin.client.components.modal._
import geotrellis.admin.client.components.sidebar._
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry

object AppCSS {
  val registry = GlobalRegistry

  def apply = registry

  def load() = {
    registry.register(
      ColorRampList.Style,
      InfoPanel.Style,
      BootstrapStyles
    )
    registry.addToDocumentOnRegistration()
  }
}
