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

import geotrellis.admin.shared._


object LayerList {

  class Backend($: BackendScope[ModelProxy[LayerModel], Unit]) {
    def onMount(proxy: ModelProxy[LayerModel]) =
      Callback.when(proxy().layers.isEmpty)(proxy.dispatch(RefreshLayers))

    def layerSelected(proxy: ModelProxy[LayerModel], layers: Seq[LayerDescription])(e: ReactEventI) = {
      import geotrellis.admin.client.circuit.Catalog._
      println(layerNameReader.value, breakCountReader.value, colorRampReader.value)
      proxy.dispatch(SelectLayer(layers.find(_.name == e.target.value)))
    }

    def render(proxy: ModelProxy[LayerModel]) = {
      //println(proxy().layers)
      <.div(^.id := "layerViewer", ^.className := "topbar",
        proxy().layers.renderPending(_ > 500, _ => <.p("Loading...")),
        proxy().layers.renderFailed(ex => <.p("Failed to load")),
        proxy().layers.render(ls => {
          <.select(
            ^.className := "layerOptions",
            dataLiveSearch := "true",
            ^.title := "Select a layer",
            ^.onChange ==> layerSelected(proxy, ls),
            ls.map { layer =>
              <.option(^.value := layer.name, ^.className := "",
                layer.name
              )
            }
          )
        })
      )
    }
  }



  private val layerViewer = ReactComponentB[ModelProxy[LayerModel]]("LayerViewer")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.onMount(scope.props))
    .build

  def apply(props: ModelProxy[LayerModel]) = layerViewer(props)
}
