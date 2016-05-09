package geotrellis.admin.client.components.map

import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import io.circe._
import io.circe.scalajs._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js._
import org.scalajs.dom.Event
import js.JSConverters._

import geotrellis.admin.client.facades._
import geotrellis.admin.client.circuit._

object LeafletMap {

  var lmap: js.UndefOr[LMap] =
    js.undefined

  var gtLayer: js.UndefOr[LTileLayer] =
    js.undefined

  val defaultMapOptions =
    LMapOptions
      .center(LLatLng(41.850033, -87.6500523))
      .zoom(AppCircuit.zoom(_.displayM.leafletM.zoom).value.getOrElse(2))
      .result

  def tileLayerOpts(minZoom: Int, maxZoom: Int) =
    LTileLayerOptions
      .errorTileUrl("/gt/errorTile")
      .minZoom(minZoom)
      .maxZoom(maxZoom)
      .result

  def updateMap = Callback {
    val displayModel = AppCircuit.zoom(_.displayM).value
    for {
      layer <- displayModel.layer
      template <- displayModel.leafletM.url
      minZoom = layer.availableZooms.min
      maxZoom = layer.availableZooms.max
    } yield {
      if (!js.isUndefined(gtLayer)) lmap.get.removeLayer(gtLayer.get)
      gtLayer = LTileLayer(template, tileLayerOpts(minZoom, maxZoom))
      gtLayer.get.addTo(lmap.get)
    }
  }

  def baseLayerOpts(maxZoom: Int, attrib: String) =
    LTileLayerOptions
      .maxZoom(maxZoom)
      .attribution(attrib)
      .result

  val layers = Map(
    "stamen" -> Map(
      "toner_lite" -> "http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png",
      "terrain" -> "http://{s}.tile.stamen.com/terrain/{z}/{x}/{y}.png",
      "watercolor" -> "http://{s}.tile.stamen.com/watercolor/{z}/{x}/{y}.png",
      "attrib" -> "Map tiles by <a href=\"http://stamen.com\">Stamen Design</a>, <a href=\"http://creativecommons.org/licenses/by/3.0\">CC BY 3.0</a> &mdash; Map data &copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a>"
    ),
    "mapbox" -> Map(
      "azavea" -> "http://{s}.tiles.mapbox.com/v3/azavea.map-zbompf85/{z}/{x}/{y}.png",
      "worldGlass" -> "http://{s}.tiles.mapbox.com/v3/mapbox.world-glass/{z}/{x}/{y}.png",
      "worldBlank" -> "http://{s}.tiles.mapbox.com/v3/mapbox.world-blank-light/{z}/{x}/{y}.png",
      "worldLight" -> "http://{s}.tiles.mapbox.com/v3/mapbox.world-light/{z}/{x}/{y}.png",
      "attrib" -> "Map data &copy; <a href=\"http://openstreetmap.org\">OpenStreetMap</a> contributors, <a href=\"http://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, Imagery &copy; <a href=\"http://mapbox.com\">MapBox</a>"
    )
  )

  def getLayer(url: String, attrib: String): LTileLayer = {
    LTileLayer(url, baseLayerOpts(18, attrib));
  };

  class Backend($: BackendScope[ModelProxy[LeafletModel], Unit]) extends OnUnmount {
    val sOnZoom = { e: LDragEndEvent => Callback.info(e) }
    val onZoom: js.Function1[LDragEndEvent, Any] = sOnZoom.asInstanceOf[js.Function1[LDragEndEvent, Any]]
    def init = Callback {
      lmap = Leaflet.map("map", defaultMapOptions)
      lmap.get.onZoomend({ e: LDragEndEvent =>
        ($.props >>=
          { proxy: ModelProxy[LeafletModel] => proxy.dispatch(UpdateZoomLevel(Some(lmap.get.getZoom()))) }
        ).runNow()
      })
      getLayer(layers("stamen")("toner_lite"), layers("stamen")("attrib")).addTo(lmap.get)
    }

    def render() =
      <.div(^.id := "map")
  }

  private val leafletMap = ReactComponentB[ModelProxy[LeafletModel]]("LeafletMap")
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .componentDidUpdate(_ => updateMap)
    .build

  def apply(proxy: ModelProxy[LeafletModel]) = leafletMap(proxy)
}

