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

  case class Selection(value: js.UndefOr[ReactNode] = {
    println("INITIALIZED AS UNDEF")
    js.undefined
  })

  class LayerBackend($: BackendScope[ModelProxy[LayerModel], Selection]) {

    def onMount(proxy: ModelProxy[LayerModel]) =
      Callback.when(proxy().layers.isEmpty)(proxy.dispatch(RefreshLayers))

//    def onSelect(layer: Option[LayerDescription]) =
//      proxy.dispatch(SelectLayer(layer))

    def onChange(options: Seq[LayerDescription], value: ReactNode): Callback = {
      println("ON CHANGE!")

      val selection = JSON.parse(JSON.stringify(value))
      val layerIndex: Int = selection.value.asInstanceOf[Int]

      $.modState(_.copy(value = value)) >>
      $.props >>= { proxy: ModelProxy[LayerModel] =>
        proxy.dispatch(SelectLayer(Try(options(layerIndex)).toOption))
      }
    }

    def render(proxy: ModelProxy[LayerModel]) = {
        <.div(
        <.h3("Layer"),
        proxy().layers.renderPending(_ > 500, _ => <.p("Loading...")),
        proxy().layers.renderFailed(ex => <.p("Failed to load")),
        proxy().layers.render(layers => {
          <.div({
            /*
            LayerForm(
              LayerForm.Props(
                options = layers.sortBy(_.name),
                onSelect = layer => proxy.dispatch(SelectLayer(layer))
              )
            )
             */

            // TODO The state is resetting itself to `undefined`.

            println(s"STATE: ${state.value}")

            val layDs = layers.sortBy(_.name)
            val options = layDs.zipWithIndex.map { case (option, index) =>
              ValueOption[ReactNode](value = index, label = option.name)
            }.toJSArray

            Select(
              options = options
              value = state.value,
              onChange = { rn: ReactNode => onChange(layDs, rn) },
              placeholder = "Select a layer to view...".asInstanceOf[ReactNode]
            )()
          })
        })
      )
    }
  }

  private val layerViewer = ReactComponentB[ModelProxy[LayerModel]]("LayerViewer")
    .initialState(Selection())
    .renderBackend[LayerBackend]
    .componentDidMount(scope => scope.backend.onMount(scope.props))
    .build

  def apply(proxy: ModelProxy[LayerModel]) = layerViewer(proxy)
}

/*
object LayerForm {

  case class Props(options: Seq[LayerDescription], onSelect: Option[LayerDescription] => Callback)

  class Backend($: BackendScope[Props, State]) {

    def render(state: State, props: Props) = {

    }
  }

  private val component = ReactComponentB[Props]("SelectForm")

    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}
 */
