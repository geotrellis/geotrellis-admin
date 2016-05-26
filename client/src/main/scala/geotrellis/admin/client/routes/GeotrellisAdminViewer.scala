package geotrellis.admin.client.routes

import diode.react._
import diode.react.ReactPot._
import geotrellis.admin.client.circuit._
import geotrellis.admin.client.components._
import geotrellis.admin.client.components.map._
import geotrellis.admin.client.components.modal._
import geotrellis.admin.client.components.sidebar._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._


object GeotrellisAdminViewer {

  case class State(showModal: Boolean = true, showJSON: Boolean = false)

  class Backend($: BackendScope[ModelProxy[RootModel], State]) {

    val onModalClose =
      $.modState(_.copy(showModal = false))

    val onModalAccept =
        $.props >>= { proxy: ModelProxy[RootModel] => proxy.dispatch(UpdateDisplay) }

    def render(props: ModelProxy[RootModel], state: State) = {
      <.div(
        ClientCircuit.wrap(_.displayM.leafletM)(LeafletMap(_)),
        <.div(
          ^.className := "sidebar",
          <.button(
            BootstrapStyles.buttonDefaultBlock,
            ^.onClick --> $.modState(_.copy(showModal = true)),
            "Map Settings"
          ),
          <.button(
            BootstrapStyles.buttonDefaultBlock,
            ^.onClick --> $.modState(s => s.copy(showJSON = !s.showJSON)),
            "Metadata JSON"
          ),
          (if (state.showJSON) {
            props().displayM.rawMetadata.render(j => <.pre(j))
          } else ""),
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
