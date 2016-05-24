package geotrellis.admin.client.components.modal

import scala.scalajs.js

import diode.react._
import geotrellis.admin.client.circuit._
import geotrellis.admin.client.components._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

object ColorOpacity {

  class Backend($: BackendScope[ModelProxy[ColorModel], Unit]) {

    def render(proxy: ModelProxy[ColorModel]) = {
      val opacity = proxy().opacity

      <.div(
        BootstrapStyles.formGroup,
        <.label(
          ^.style := js.Dictionary("opacity" -> s"${0.01 * opacity}"),
          ^.htmlFor := "opacitySelect",
          s"Opacity: ${opacity}%"
        ),
        <.input.range(
          ^.id := "opacitySelect",
          ^.min := "0",
          ^.max := "100",
          ^.step := 1,
          ^.onChange ==> { (e: ReactEventI) => proxy.dispatch(SetOpacity(e.target.value.toInt)) },
          ^.value := opacity
        )
      )
    }
  }

  private val colorOpacitySelect = ReactComponentB[ModelProxy[ColorModel]]("ColorOpacitySelect")
    .renderBackend[Backend]
    .build

  def apply(props: ModelProxy[ColorModel]) = colorOpacitySelect(props)
}
