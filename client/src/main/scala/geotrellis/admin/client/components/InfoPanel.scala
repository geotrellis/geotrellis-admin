package geotrellis.admin.client.components

import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.vdom.Extra
import org.scalajs.dom
import scalacss.Defaults._
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._


object InfoPanel {

  class Backend($: BackendScope[Unit, Unit]) {
    def render = {
      <.div()
    }
  }

  private val component = ReactComponentB[Unit]("InfoPanel")
    .renderBackend[Backend]
    .build

  def apply = component()

}
