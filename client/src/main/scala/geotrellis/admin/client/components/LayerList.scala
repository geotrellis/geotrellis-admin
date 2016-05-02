package geotrellis.admin.client.components

import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.reactselect._
import chandu0101.scalajs.react.components._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._
import org.scalajs.dom


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
      <.div(
        proxy().layers.renderPending(_ > 500, _ => <.p("Loading...")),
        proxy().layers.renderFailed(ex => <.p("Failed to load")),
        proxy().layers.render(ls => {
          <.div(
            <.h3("Layer"),
            LayerForm(
              LayerForm.Props(
                options = ls,
                onSelect = l => proxy.dispatch(SelectLayer(Some(l)))
              )
            )
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

object LayerForm {

  case class Props(options: Seq[LayerDescription], onSelect: LayerDescription => Callback, selected: Option[LayerDescription] = None)
  case class State(value: js.UndefOr[ReactNode] = js.undefined)

  class Backend($: BackendScope[Props, State]) {

    def onChange(value: ReactNode) = {
      $.modState(_.copy(value = value)) >>
        Callback.info(s"Chosen ${value}")

    }

    def render(state: State, props: Props) = {
      val optsWithIndex = props.options.zipWithIndex

      def changeFunc(r: ReactNode): Unit = {
      }

      val options = optsWithIndex.map { case (option, index) =>
          ValueOption[ReactNode](value = index, label = option.name)
      }.toJSArray

      Select(
        options = options,
        value = state.value,
        onChange = onChange _,
        placeholder = "Select a layer to view...".asInstanceOf[js.Any]
      )()
    }
  }

  val component = ReactComponentB[Props]("ReactSelectDemo")
    .initialState_P(p => {
      val selected: js.UndefOr[ReactNode] = p.selected match {
        case None => js.undefined
        case Some(l) => l.name.asInstanceOf[ReactNode]
      }
      State(value = selected)
    })
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}
