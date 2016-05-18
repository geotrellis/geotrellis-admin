package geotrellis.admin.client.components.sidebar

import diode.react._
import diode.react.ReactPot._
import geotrellis.admin.client.circuit._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLDivElement
import scalacss.Defaults._

object InfoPanel {

  object Style extends StyleSheet.Inline {

  }

  class Backend($: BackendScope[ModelProxy[DisplayModel], Unit]) {
    def render(proxy: ModelProxy[DisplayModel]): ReactTagOf[HTMLDivElement] = {
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
        }),
        proxy().attributes.render(attrs => {
          <.div(
            <.h3("Extra Attributes"),
            attrs.attrs.map({ case (k,v) =>
              <.div(
                <.h4(s"${k}"),
                <.p(s"${v}")
              )
            })
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
