package geotrellis.admin.client.routes

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.{UndefOr, undefined}

import geotrellis.admin.client.components._
import geotrellis.admin.client.circuit.AppCircuit


object TopDashboard {

  val dashboardDom =
    <.nav(^.className := "navbar-default navbar-fixed-top",
      <.div(^.className := "container",
        <.div(^.className := "row",
          AppCircuit.connect(_.layerM)(LayerList(_)),
          ColorRampList()
        )
      )
    )
  //.connect(_.todos.map(_.items.count(!_.completed)).toOption)(proxy => MainMenu(c, r.page, proxy))

  private val dashboard = ReactComponentB[Unit]("dashboard")
    .render(_ => dashboardDom)
    .build

  def apply() = dashboard()

}
