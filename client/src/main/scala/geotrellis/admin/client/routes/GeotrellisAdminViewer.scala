package geotrellis.admin.client.routes

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.{UndefOr, undefined}

import geotrellis.admin.client.components._
import geotrellis.admin.client.circuit._


object GeotrellisAdminViewer {

  case class State(showModal: Boolean = true)

  class Backend($: BackendScope[Unit, State]) {
    def render(state: State) = {
      <.div(
        AppCircuit.wrap(_.displayM.leafletM)(LeafletMap(_)),
        AppCircuit.connect(_.displayM)(InfoPanel(_)),
        if (state.showModal) AppCircuit.wrap(_.displayM)(SetupModal(_))
        else Seq.empty[ReactElement]
      )
    }
  }

  private val component = ReactComponentB[Unit]("GeotrellisAdminClient")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component()

}

