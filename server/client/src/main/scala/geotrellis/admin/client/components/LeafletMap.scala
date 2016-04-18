package geotrellis.admin.client.components

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

import geotrellis.admin.client.facades._

object LeafletMap {

  val defaultMapOptions =
    LMapOptions
      .center(LLatLng(40.75583970971843, -73.795166015625))
      .zoom(11)
      .result

  def tileLayerOpts(maxZoom: Int, attrib: String) =
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
    LTileLayer(url, tileLayerOpts(18, attrib));
  };

  class Backend($: BackendScope[Unit, Unit]) {
    var map: js.UndefOr[LMap] =
      js.undefined

    def init = Callback {
      map = Leaflet.map("map", defaultMapOptions)
      getLayer(layers("stamen")("toner_lite"), layers("stamen")("attrib")).addTo(map.get)
    }

    def clear = Callback {
    }

    def render() =
      <.div(
        ^.id := "map"
      )
  }

  private val leafletMap = ReactComponentB[Unit]("LeafletMap")
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .build

  def apply() = leafletMap()
}

