package geotrellis.admin.client.components

import diode._
import diode.react._
import diode.data.Pot
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import scalacss.Defaults._
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._
import scala.util.Try

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._

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
