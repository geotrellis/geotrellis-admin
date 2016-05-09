package geotrellis.admin.client

import chandu0101.scalajs.react.components._
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry

import geotrellis.admin.client.components.map._
import geotrellis.admin.client.components.modal._
import geotrellis.admin.client.components.sidebar._
import geotrellis.admin.client.routes._
import geotrellis.admin.client.components._

object AppCSS {
  val registry = GlobalRegistry

  def apply = registry

  def load() = {
    registry.register(
      GeotrellisAdminViewer.Style,
      ColorRampList.Style,
      InfoPanel.Style,
      BootstrapStyles
    )
    registry.addToDocumentOnRegistration()
  }
}
