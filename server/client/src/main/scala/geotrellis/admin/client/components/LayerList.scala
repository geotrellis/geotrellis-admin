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


object LayerList {

  def layerViewSuccess(props: Seq[Layer]) =
    <.div(^.id := "layerViewer", ^.className := "topbar",
      <.select(^.className := "layerOptions",
        <.option(), props.map { layer =>
        <.option(^.value := layer.name, ^.className := "",
          layer.name
        )
      })
    )


  /** callback to set up the select */
  def onUpdate(x: Any): Callback = Callback {
    val jq = js.Dynamic.global.$
    jq(".layerOptions").select2(js.Dictionary("placeholder" -> "Select a layer"))
  }

  class Backend($: BackendScope[ModelProxy[LayerModel], Unit]) {
    def mounted(proxy: ModelProxy[LayerModel]) =
      // dispatch a message to refresh the todos, which will cause TodoStore to fetch todos from the server
      Callback.when(proxy().layers.isEmpty)(proxy.dispatch(InitLayers))

    def render(proxy: ModelProxy[LayerModel]) = {
      //println(proxy().layers)
      <.div(^.className := "topbar",
        proxy().layers.renderPending(_ > 500, _ => <.p("Loading...")),
        proxy().layers.renderFailed(ex => <.p("Failed to load")),
        proxy().layers.render(ls => {
          layerViewSuccess(ls)
        })
      )
    }
  }



  private val layerViewer = ReactComponentB[ModelProxy[LayerModel]]("LayerViewer")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .componentDidMount(onUpdate)
    .componentDidUpdate(onUpdate)
    .build

  def apply(props: ModelProxy[LayerModel]) = layerViewer(props)
}
