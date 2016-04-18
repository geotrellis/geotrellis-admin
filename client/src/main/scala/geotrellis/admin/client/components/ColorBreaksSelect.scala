package geotrellis.admin.client.components

import diode._
import diode.react._
import diode.data.Pot
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._
import scala.util.Try

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._

object ColorBreaksSelect {

  class Backend($: BackendScope[ModelProxy[ColorBreaksModel], Unit]) {
    def breakCountSelected(proxy: ModelProxy[ColorBreaksModel])(e: ReactEventI) ={
      proxy.dispatch(SelectBreakCount(Try(e.target.value.toInt).toOption))}

    def render(proxy: ModelProxy[ColorBreaksModel]) =
      <.div(^.id := "breaksCount", ^.className := "topbar",
        <.input.number(^.id := "break-count", ^.onChange ==> breakCountSelected(proxy))
      )
  }

  private val colorRampSelect = ReactComponentB[ModelProxy[ColorBreaksModel]]("ColorRampSelect")
    .renderBackend[Backend]
    .build

  def apply(props: ModelProxy[ColorBreaksModel]) = colorRampSelect(props)
}
