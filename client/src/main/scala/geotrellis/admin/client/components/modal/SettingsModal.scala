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

  val settings = ReactComponentB[Props]("LayerSettings").render_P({ props =>
    <.div(
      Modal(
        Modal.Props(
          header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> (props.onClose >> hide), "x"), <.h2("Layer Rendering Options")),
          footer = hide => <.span(Button(Button.Props(props.onAccept >> hide), "OK")),
          closed = props.onClose
        ),
        ClientCircuit.connect(_.layerM)(LayerList.layerSelect(_)),
        ClientCircuit.connect(_.colorM)(ColorRampList.colorRampSelect(_)),
        ClientCircuit.connect(_.colorM)(ColorOpacity.colorOpacitySelect(_)),
        ClientCircuit.connect(_.breaksM)(BreaksCount.breakSelect(_))
      )
    )
  }).build

}
