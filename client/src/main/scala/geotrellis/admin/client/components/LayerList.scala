package geotrellis.admin.client.components

import diode._
import diode.react._
import diode.data.Pot
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.reactselect._
import chandu0101.scalajs.react.components._

import scala.scalajs.js.JSON
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._
import org.scalajs.dom

import scala.collection.mutable
import scala.util.Try

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._
import geotrellis.admin.shared._


object LayerList {

  class Backend($: BackendScope[ModelProxy[LayerModel], Unit]) {
    def onMount(proxy: ModelProxy[LayerModel]) =
      Callback.when(proxy().layers.isEmpty)(proxy.dispatch(RefreshLayers))

    def render(proxy: ModelProxy[LayerModel]) = {
      //println(proxy().layers)
      <.div(
        <.h3("Layer"),
        proxy().layers.renderPending(_ > 500, _ => <.p("Loading...")),
        proxy().layers.renderFailed(ex => <.p("Failed to load")),
        proxy().layers.render(layers => {
          <.div(
            LayerForm(
              LayerForm.Props(
                options = layers.sortBy(_.name),
                onSelect = layer => proxy.dispatch(SelectLayer(layer))
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

  case class Props(options: Seq[LayerDescription], onSelect: Option[LayerDescription] => Callback)
  case class State(value: js.UndefOr[ReactNode] = js.undefined)

  class Backend($: BackendScope[Props, State]) {

    def onChange(value: ReactNode) = {
      val selection = JSON.parse(JSON.stringify(value))
      val layerIndex: Int = selection.value.asInstanceOf[Int]

      $.modState(_.copy(value = value)) >>
        Callback.info(selection) >>
        $.props >>= { (props: Props) => props.onSelect(Try(props.options(layerIndex)).toOption) }
    }

    def render(state: State, props: Props) = {
      val options =
        props.options.zipWithIndex.map { case (option, index) =>
          ValueOption[ReactNode](value = index, label = option.name)
        }.toJSArray

      Select(
        options = options,
        value = state.value,
        onChange = onChange _,
        placeholder = "Select a layer to view...".asInstanceOf[ReactNode]
      )()
    }
  }

  private val component = ReactComponentB[Props]("ReactSelectDemo")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}
