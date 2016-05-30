package geotrellis.admin.client.components.modal

import chandu0101.scalajs.react.components.JsCol
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON
import scala.util.Try

import chandu0101.scalajs.react.components.reactselect._
import chandu0101.scalajs.react.components.Implicits._
import diode.react._
import diode.react.ReactPot._
import geotrellis.admin.client.circuit._
import geotrellis.admin.shared._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._


object LayerList {

  class LayerBackend($: BackendScope[ModelProxy[LayerModel], Unit]) {

    def onMount(proxy: ModelProxy[LayerModel]) =
      Callback.when(proxy().layers.isEmpty)(proxy.dispatch(RefreshLayers))

    def onChange(options: Seq[LayerDescription], value: ReactNode): Callback = {
      val selection = JSON.parse(JSON.stringify(value))
      val layerIndex: Int = selection.value.asInstanceOf[Int]

      $.props >>= { proxy: ModelProxy[LayerModel] =>
        proxy.dispatch(SelectLayer(Try(options(layerIndex)).toOption))
      }
    }

    def render(proxy: ModelProxy[LayerModel]) = {
      <.div(
        <.h3("Layer"),
        proxy().layers.renderEmpty(<.p("Fetching layers...")),
        proxy().layers.renderFailed(ex => <.p("Failed to load")),
        proxy().layers.render(layers => {
          <.div({
            val layDs: Seq[LayerDescription] = layers.sortBy(_.name)

            val options: Seq[ValueOption[ReactNode]] = layDs.zipWithIndex.map { case (option, index) =>
              ValueOption[ReactNode](value = index, label = option.name)
            }

            // For some reason, `.getOrElse` won't compile here.
            val sel: JsCol[ReactNode] = proxy().selection.flatMap(ld =>
              options.filter(_.label == ld.name).headOption.map(_.value)  // Naive.
            ) match {
              case Some(rn) => rn
              case None => options.head.value
            }

            Select(
              options = options.toJsArray,
              value = sel,
              onChange = { rn: ReactNode => onChange(layDs, rn) },
              placeholder = "Select a layer to view...".asInstanceOf[ReactNode]
            )()
          })
        })
      )
    }
  }

  private val layerViewer = ReactComponentB[ModelProxy[LayerModel]]("LayerViewer")
    .renderBackend[LayerBackend]
    .componentDidMount(scope => scope.backend.onMount(scope.props))
    .build

  def apply(proxy: ModelProxy[LayerModel]) = layerViewer(proxy)
}
