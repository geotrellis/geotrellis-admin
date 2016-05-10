package geotrellis.admin.client.components.modal

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

object SettingsModal {
  @inline private def bss = BootstrapStyles

  case class Props(onAccept: Callback, onClose: Callback)
  case class State(layer: Option[LayerDescription] = None)

  class Backend($: BackendScope[Props, State]) {

    def onLayerSelect(ld: Option[LayerDescription]) = {
      $.modState(_.copy(layer = ld))
    }

    def render(props: Props) = {
      <.div(
        Modal(
          Modal.Props(
            header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> (props.onClose >> hide), "x"), <.h2("Layer rendering options")),
            footer = hide => <.span(Button(Button.Props(props.onAccept >> hide), "OK")),
            closed = props.onClose
          ),
          AppCircuit.connect(_.layerM)(LayerList(_)),
          AppCircuit.connect(_.colorM)(ColorRampList(_)),
          AppCircuit.connect(_.colorM)(ColorOpacity(_)),
          AppCircuit.connect(_.breaksM)(BreaksCount(_))
        )
      )
    }
  }

  private val component = ReactComponentB[Props]("dashboard")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)

}
