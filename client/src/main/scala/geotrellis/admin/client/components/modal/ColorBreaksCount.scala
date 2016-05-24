package geotrellis.admin.client.components.modal

import scala.util.Try

import diode.react._
import geotrellis.admin.client.circuit._
import geotrellis.admin.client.components._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

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
