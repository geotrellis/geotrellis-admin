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

  def colorRampSelectDom =
    <.div(^.id := "colorViewer", ^.className := "topbar",
      <.select(^.className := "colorOptions", colorRamps.map { ramp =>
        <.option(^.value := ramp, ^.style := js.Dictionary("backgroundImage" -> ("url(img/ramps/" + ramp + ".png)")),
          //ramp
          <.img(^.href := "img/ramps/" + ramp + ".png")
        )
      })
    )

  class Backend($: BackendScope[Unit, Unit]) {
    def render() = colorRampSelectDom
  }

  private val colorRampSelect = ReactComponentB[Unit]("ColorRampSelect")
    .renderBackend[Backend]
    .build

  def apply() = colorRampSelect()
}
