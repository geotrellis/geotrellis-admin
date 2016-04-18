package geotrellis.admin.client.components

import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._

import org.querki.jquery._

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._

object ColorRampList {

  val colorRamps = List[String](
    "blue-to-orange",
    "blue-to-red",
    "blue-to-yellow-to-red-heatmap",
    "bold-land-use-qualitative",
    "dark-red-to-yellow-heatmap",
    "green-to-orange",
    "green-to-red-orange",
    "light-to-dark-green",
    "light-to-dark-sunset",
    "muted-terrain-qualitative",
    "purple-to-dark-purple-to-white-heatmap",
    "yellow-to-red-heatmap"
  )

  class Backend($: BackendScope[ModelProxy[ColorRampModel], Unit]) {
    def onMount(proxy: ModelProxy[ColorRampModel]) =
      proxy.dispatch(SelectColorRamp(colorRamps.headOption))

    def rampSelected(proxy: ModelProxy[ColorRampModel])(e: ReactEventI) =
      proxy.dispatch(SelectColorRamp(colorRamps.find(_ == e.target.value)))

    def render(proxy: ModelProxy[ColorRampModel]) =
      <.div(^.id := "colorViewer", ^.className := "topbar",
        <.select(^.id := "colorOptions", ^.onChange ==> rampSelected(proxy),
          colorRamps.map { ramp =>
            <.option(^.value := ramp, ramp) //"""<img src=\"img/ramps/${ramp}.png\">""")
          }
        ), proxy().selection.map(ramp => <.img(^.src := s"img/ramps/${ramp}.png"))
      )
  }

  private val colorRampSelect = ReactComponentB[ModelProxy[ColorRampModel]]("ColorRampSelect")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.onMount(scope.props))
    .build

  def apply(props: ModelProxy[ColorRampModel]) = colorRampSelect(props)
}
