package geotrellis.admin.client.routes

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.annotation.JSExport

import geotrellis.admin.client.components.sidebar._
import geotrellis.admin.client.components.modal._
import geotrellis.admin.client.components.map._
import geotrellis.admin.client.components._
import geotrellis.admin.client.circuit._


object GeotrellisAdminViewer {

  case class State(showModal: Boolean = true)

  class Backend($: BackendScope[ModelProxy[RootModel], State]) {

    val onModalClose =
      $.modState(_.copy(showModal = false))

    val onModalAccept =
        $.props >>= { proxy: ModelProxy[RootModel] => proxy.dispatch(UpdateDisplay) }

    def render(props: ModelProxy[RootModel], state: State) = {
      <.div(
        AppCircuit.wrap(_.displayM.leafletM)(LeafletMap(_)),
        <.div(
          ^.className := "sidebar",
          <.button(
            BootstrapStyles.buttonDefaultBlock,
            ^.onClick --> $.modState(_.copy(showModal = true)),
            "Layer Settings"
          ),
          <.div(
            props.connect(_.displayM)(InfoPanel(_))
          ),
          if (state.showModal) SettingsModal(SettingsModal.Props(onModalAccept, onModalClose))
          else Seq.empty[ReactElement]
        )
      )
    }
  }

  private val component = ReactComponentB[ModelProxy[RootModel]]("GeotrellisAdminClient")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(props: ModelProxy[RootModel]) = component(props)

}

