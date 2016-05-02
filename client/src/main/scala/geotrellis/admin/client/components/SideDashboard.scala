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
import scalacss.ScalaCssReact._

import geotrellis.admin.client.components._
import Bootstrap._
import geotrellis.admin.client.circuit.AppCircuit
import geotrellis.admin.client.style.Style
import scalacss.ScalaCssReact._

object SideDashboard {
  @inline private def bss = GlobalStyles.bootstrapStyles

  val dashboardDom =
    <.div(^.className := "sidebar",
      Modal(
        Modal.Props(
          header = hide => <.span(<.button(Style.red, ^.tpe := "button", bss.close.htmlClass, ^.onClick --> hide, Icon.close), <.h4("headertext")),
          footer = hide => <.span(Button(Button.Props(Callback("footerbutton hit, hiding next") >> hide), "OK")),
          closed = Callback(println("IT IS HAPPENING"))
        ),
        <.div(
          AppCircuit.connect(_.layerM)(LayerList(_)),
          AppCircuit.connect(_.colorM)(ColorRampList(_)),
          AppCircuit.connect(_.breaksM)(ColorBreaksSelect(_)),
          ReactSelectDemo()
        )
      )
    )
  //.connect(_.todos.map(_.items.count(!_.completed)).toOption)(proxy => MainMenu(c, r.page, proxy))

  private val dashboard = ReactComponentB[Unit]("dashboard")
    .render(_ => dashboardDom)
    .build

  def apply() = dashboard()

}
