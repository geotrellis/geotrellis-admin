package geotrellis.admin.client.components.modal

import geotrellis.admin.client.circuit._
import geotrellis.admin.client.components._
import geotrellis.admin.client.components.Bootstrap._
import geotrellis.admin.shared._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

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
            header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> (props.onClose >> hide), "x"), <.h2("Layer Rendering Options")),
            footer = hide => <.span(Button(Button.Props(props.onAccept >> hide), "OK")),
            closed = props.onClose
          ),
          ClientCircuit.connect(_.layerM)(LayerList(_)),
          ClientCircuit.connect(_.colorM)(ColorRampList(_)),
          ClientCircuit.connect(_.colorM)(ColorOpacity(_)),
          ClientCircuit.connect(_.breaksM)(BreaksCount(_))
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
