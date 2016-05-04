package geotrellis.admin.client.routes

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import diode._
import diode.react._
import diode.data.Pot
import diode.react.ReactPot._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.{UndefOr, undefined}
import scalacss.ScalaCssReact._

import geotrellis.admin.client.components._
import geotrellis.admin.client.components.Bootstrap._
import geotrellis.admin.client.circuit._
import geotrellis.admin.shared._

object SetupModal {
  @inline private def bss = BootstrapStyles

  case class State(layer: Option[LayerDescription] = None)

  class Backend($: BackendScope[ModelProxy[DisplayModel], State]) {

    def render(proxy: ModelProxy[DisplayModel]) = {
      <.div(
        Modal(
          Modal.Props(
            header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> hide, "x"), <.h2("Layer rendering options")),
            footer = hide => <.span(Button(Button.Props(proxy.dispatch(UpdateDisplay) >> hide), "OK")),
            closed = Callback.info("Closing modal")
          ),
          AppCircuit.connect(_.layerM)(LayerList(_)),
          AppCircuit.connect(_.colorM)(ColorRampList(_)),
          AppCircuit.connect(_.colorM)(ColorOpacity(_)),
          AppCircuit.connect(_.breaksM)(BreaksCount(_))
        )
      )
    }
  }

  private val component = ReactComponentB[ModelProxy[DisplayModel]]("dashboard")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[DisplayModel]) = component(proxy)

}
