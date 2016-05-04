package geotrellis.admin.client.components

import diode._
import diode.react._
import diode.data.Pot
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.vdom.Extra
import org.scalajs.dom
import scalacss.Defaults._
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation._
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.JSConverters._

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._


object InfoPanel {

  class Backend($: BackendScope[ModelProxy[DisplayModel], Unit]) {
    def render(proxy: ModelProxy[DisplayModel]) = {

      println(proxy().metadata)

      <.div(^.className := "sidebar",
        proxy().metadata.render(md => {
          <.div(
            <.h2("Metadata"),
            <.h4("Extent"),
            <.div(
              <.p(md.extent.xmin),
              <.p(md.extent.ymin),
              <.p(md.extent.xmax),
              <.p(md.extent.ymax)
            ),
            <.h4("Layout Definition"),
            <.div(
              <.h5("Definition Extent"),
              <.div(
                <.p(md.layoutDefinition.extent.xmin),
                <.p(md.layoutDefinition.extent.ymin),
                <.p(md.layoutDefinition.extent.xmax),
                <.p(md.layoutDefinition.extent.ymax)
              ),
              <.h5("Tile Layout"),
              <.div(
                <.p(md.layoutDefinition.tileLayout.layoutCols),
                <.p(md.layoutDefinition.tileLayout.layoutRows),
                <.p(md.layoutDefinition.tileLayout.tileCols),
                <.p(md.layoutDefinition.tileLayout.tileRows)
              )
            ),
            <.h4("Bounds"),
            <.div(
              <.p(md.bounds.minKey.toString),
              <.p(md.bounds.maxKey.toString)
            ),
            <.h4("CellType"),
            <.p(md.cellType),
            <.h4("CRS"),
            <.p(md.crs)
          )
        })
      )
    }
  }

  private val component = ReactComponentB[ModelProxy[DisplayModel]]("InfoPanel")
    .renderBackend[Backend]
    .build

  def apply(props: ModelProxy[DisplayModel]) = component(props)

}
