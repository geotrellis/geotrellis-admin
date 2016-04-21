package geotrellis.admin.client.components

import diode.react.ReactPot._
import diode._
import diode.react._
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
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
      .center(LLatLng(40.75583970971843, -73.795166015625))
      .zoom(4)
      .result

  def updateTile = Callback {
    val urlTemplate = for {
      lDescription <- AppCircuit.zoom(_.layerM.selection).value
      cmapName <- AppCircuit.zoom(_.colorM.selection).value
    } yield s"""http://0.0.0.0:8080/gt/tms/${lDescription.name}/{z}/{x}/{y}?colorRamp=${cmapName}&breaks=0,1,3,5,8,10,20,30,40,50,60,80,100,200"""

    urlTemplate.map { template =>
      if (!js.isUndefined(gtLayer)) lmap.get.removeLayer(gtLayer.get)
      gtLayer = LTileLayer(template)
      gtLayer.get.addTo(lmap.get)
    }
  }

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

  class Backend($: BackendScope[ModelProxy[RootModel], Unit]) {

    def init = Callback {
      lmap = Leaflet.map("map", defaultMapOptions)
      getLayer(layers("stamen")("toner_lite"), layers("stamen")("attrib")).addTo(lmap.get)
    }


    def render() =
      <.div(
        ^.id := "map"
      )
  }

  private val leafletMap = ReactComponentB[ModelProxy[RootModel]]("LeafletMap")
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .componentDidUpdate(_ => updateTile)
    .build

  def apply(props: ModelProxy[RootModel]) = leafletMap(props)
}

