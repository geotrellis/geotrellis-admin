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

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._

import japgolly.scalajs.react.vdom.Extra


object ColorRampList {

  val colorRamps = Map[String, Array[String]](
    "blue-to-orange" -> Array(
      "#2586AB", "#4EA3C8", "#7FB8D4", "#ADD8EA",
      "#C8E1E7", "#EDECEA", "#F0E7BB", "#F5CF7D",
      "#F9B737", "#E68F2D", "#D76B27"
    ),
    "blue-to-red" -> Array(
      "#2791C3", "#5DA1CA", "#83B2D1", "#A8C5D8",
      "#CCDBE0", "#E9D3C1", "#DCAD92", "#D08B6C",
      "#C66E4B", "#BD4E2E"
    ),
    "blue-to-yellow-to-red-heatmap" -> Array(
      "#2A2E7F", "#3D5AA9", "#4698D3", "#39C6F0",
      "#76C9B3", "#A8D050", "#F6EB14", "#FCB017",
      "#F16022", "#EE2C24", "#7D1416"
    ),
    "bold-land-use-qualitative" -> Array(
      "#B29CC3", "#4F8EBB", "#8F9238", "#C18437",
      "#B5D6B1", "#D378A6", "#D4563C", "#F9BE47"
    ),
    "dark-red-to-yellow-heatmap" -> Array(
      "#68101A", "#7F182A", "#A33936", "#CF3A27",
      "#D54927", "#E77124", "#ECBE1D", "#F7DA22",
      "#F6EDB1", "#FFFFFF"
    ),
    "green-to-orange" -> Array(
      "#118C8C", "#429D91", "#61AF96", "#75C59B",
      "#A2CF9F", "#C5DAA3", "#E6E5A7", "#E3D28F",
      "#E0C078", "#DDAD62", "#D29953", "#CA8746",
      "#C2773B"
    ),
    "green-to-red-orange" -> Array(
      "#569543", "#9EBD4D", "#BBCA7A", "#D9E2B2",
      "#E4E7C4", "#E6D6BE", "#E3C193", "#DFAC6C",
      "#DB9842", "#B96230"
    ),
    "light-to-dark-green" -> Array(
      "#E8EDDB", "#DCE8D4", "#BEDBAD", "#A0CF88",
      "#81C561", "#4BAF48", "#1CA049", "#3A6D35"
    ),
    "light-to-dark-sunset" -> Array(
      "#FFFFFF", "#FBEDD1", "#F7E0A9", "#EFD299",
      "#E8C58B", "#E0B97E", "#F2924D", "#C97877",
      "#946196", "#2AB7D6", "#474040"
    ),
    "muted-terrain-qualitative" -> Array(
      "#CEE1E8", "#7CBCB5", "#82B36D", "#94C279",
      "#D1DE8D", "#EDECC3", "#CCAFB4", "#C99884FF"
    ),
    "purple-to-dark-purple-to-white-heatmap" -> Array(
      "#A52278", "#993086", "#8C3C97", "#6D328A",
      "#4E2B81", "#3B264B", "#180B11", "#FFFFFF"
    ),
    "yellow-to-red-heatmap" -> Array(
      "#F7DA22", "#ECBE1D", "#E77124", "#D54927",
      "#CF3A27", "#A33936", "#7F182A", "#68101A"
    )
  )

  class Backend($: BackendScope[ModelProxy[ColorRampModel], Unit]) {

    def onMount(proxy: ModelProxy[ColorRampModel]) =
      proxy.dispatch(SelectColorRamp(colorRamps.keys.headOption))

    def rampSelected(proxy: ModelProxy[ColorRampModel], name: String) =
      proxy.dispatch(SelectColorRamp(Some(name)))

    def render(proxy: ModelProxy[ColorRampModel]) = {
      <.ul(
        ^.className := "color-list",
        colorRamps.map { case (name, colors) =>
          <.li(
            ^.key := name,
            ^.className := "color-item",
            colors.map { color =>
              <.div( ^.key := color,
                ^.className := "color-display",
                ^.style := js.Dictionary("width" -> s"${100.0 / colors.length}%", "backgroundColor" -> color),
                ^.onClick --> rampSelected(proxy, name)
              )
            }
          )
        }
      )
    }
  }

  private val colorRampSelect = ReactComponentB[ModelProxy[ColorRampModel]]("ColorRampSelect")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.onMount(scope.props))
    .build

  def apply(props: ModelProxy[ColorRampModel]) = colorRampSelect(props)
}
