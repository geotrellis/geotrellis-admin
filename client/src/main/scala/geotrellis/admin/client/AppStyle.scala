package geotrellis.admin.client

import chandu0101.scalajs.react.components._
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry

import geotrellis.admin.client.components._

object AppCSS {
  val registry = GlobalRegistry

  def apply = registry

  def load() = {
    registry.register(
      ColorRampList.Style,
      BootstrapStyles
    )
    registry.addToDocumentOnRegistration()
  }
}
