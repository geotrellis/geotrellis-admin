package geotrellis.admin.client.components.modal

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON
import scala.util.Try

import chandu0101.scalajs.react.components.reactselect._
import diode.react._
import diode.react.ReactPot._
import geotrellis.admin.client.circuit._
import geotrellis.admin.shared._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._


object LayerList {

  class Backend($: BackendScope[ModelProxy[LayerModel], Unit]) {
    def onMount(props: ModelProxy[LayerModel]) =
      Callback.when(props().layers.isEmpty)(props.dispatch(RefreshLayers))

    def render(props: ModelProxy[LayerModel]) = {
      <.div(
        <.h3("Layer"),
        props().layers.renderPending(_ > 500, _ => <.p("Loading...")),
        props().layers.renderFailed(ex => <.p("Failed to load")),
        props().layers.render(layers => {
          <.div(
            LayerForm(
              LayerForm.Props(
                options = layers.sortBy(_.name),
                onSelect = layer => props.dispatch(SelectLayer(layer))
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
