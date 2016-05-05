package geotrellis.admin.client.components.modal

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
import geotrellis.admin.client.components._
import geotrellis.admin.client.circuit._

object BreaksCount {

  class Backend($: BackendScope[ModelProxy[BreaksModel], Unit]) {
    def render(proxy: ModelProxy[BreaksModel]) = {
      <.div(BootstrapStyles.formGroup,
        <.label(
          ^.htmlFor := "breakCount",
          "Color Breaks (used to determine the number of colors generated)"
        ),
        <.input.number(
          BootstrapStyles.formControl,
          ^.id := "breakCount",
          ^.onChange ==> { (e: ReactEventI) => proxy.dispatch(SelectBreaksCount(Try(e.target.value.toInt).toOption)) },
          ^.value := proxy().breaksCount.getOrElse(0)
        )
      )
    }
  }

  private val colorRampSelect = ReactComponentB[ModelProxy[BreaksModel]]("ColorRampSelect")
    .renderBackend[Backend]
    .build

  def apply(props: ModelProxy[BreaksModel]) = colorRampSelect(props)
}
