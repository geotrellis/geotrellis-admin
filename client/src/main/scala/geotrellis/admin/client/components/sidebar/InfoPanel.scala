package geotrellis.admin.client.components.sidebar

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

  object Style extends StyleSheet.Inline {
    import dsl._

  }

  class Backend($: BackendScope[ModelProxy[DisplayModel], Unit]) {
    def render(proxy: ModelProxy[DisplayModel]) = {
      <.div(
        proxy().metadata.render(md => {
          <.div(
            <.h3("Layer Metadata"),
            <.p(s"CellType: ${md.cellType}"),
            <.p(s"CRS: ${md.crs}"),
            <.div(
              <.h4("Layer Extent"),
              <.div(
                <.p(s"xmin: ${md.extent.xmin}"),
                <.p(s"xmin: ${md.extent.ymin}"),
                <.p(s"xmin: ${md.extent.xmax}"),
                <.p(s"xmin: ${md.extent.ymax}")
              )
            ),
            <.h4("Bounds"),
            <.div(
              <.p(s"MinKey: ${md.bounds.minKey.toString}"),
              <.p(s"MaxKey: ${md.bounds.maxKey.toString}")
            ),
            <.div(
              <.h4("Layout Definition"),
              <.div(
                <.h5("Definition Extent"),
                <.div(
                  <.p(s"xmin: ${md.layoutDefinition.extent.xmin}"),
                  <.p(s"ymin: ${md.layoutDefinition.extent.ymin}"),
                  <.p(s"xmax: ${md.layoutDefinition.extent.xmax}"),
                  <.p(s"ymax: ${md.layoutDefinition.extent.ymax}")
                ),
                <.h5("Tile Layout"),
                <.div(
                  <.p(s"Layout cols: ${md.layoutDefinition.tileLayout.layoutCols}"),
                  <.p(s"Layout rows: ${md.layoutDefinition.tileLayout.layoutRows}"),
                  <.p(s"Tile cols: ${md.layoutDefinition.tileLayout.tileCols}"),
                  <.p(s"Tile rows: ${md.layoutDefinition.tileLayout.tileRows}")
                )
              )
            )
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
