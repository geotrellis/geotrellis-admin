package geotrellis.admin.client.circuit

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._
import scala.scalajs.js

import geotrellis.admin.client.facades._
import geotrellis.admin.client._
import geotrellis.admin.shared._

// Define models
case class RootModel(
  layerM: LayerModel = LayerModel(),
  colorM: ColorModel = ColorModel(),
  breaksM: BreaksModel = BreaksModel(),
  displayM: DisplayModel = DisplayModel()
)
case class LayerModel(layers: Pot[Array[LayerDescription]] = Empty, selection: Option[LayerDescription] = None)
case class ColorModel(ramp: Option[String] = None, opacity: Int = 100)
case class BreaksModel(breaks: Pot[String] = Empty, breaksCount: Option[Int] = None)

case class LeafletModel(url: Option[String] = None, zoom: Option[Int] = None, lmap: js.UndefOr[LMap] = js.undefined) {
  def tileLayerOpts(minZoom: Int, maxZoom: Int) =
    LTileLayerOptions
      .errorTileUrl(SiteConfig.adminHostUrl("/gt/errorTile"))
      .minZoom(minZoom)
      .maxZoom(maxZoom)
      .result
}
case class DisplayModel(
  layer: Option[LayerDescription] = None,
  ramp: Option[String] = None,
  opacity: Option[Int] = None,
  breaksCount: Option[Int] = None,
  metadata: Pot[Metadata] = Empty,
  leafletM: LeafletModel = LeafletModel()
)

// Define actions
case object RefreshLayers
case class SelectLayer(layer: Option[LayerDescription])
case object DeselectLayer
case class UpdateLayers(layers: Pot[Array[LayerDescription]] = Empty)

case class SelectColorRamp(ramp: Option[String])
case class SetOpacity(opacity: Int)

case object RefreshBreaks
case class SelectBreaksCount(breaks: Option[Int])
case class UpdateBreaks(breaks: Pot[String] = Empty)

case object UpdateDisplay
case object UpdateDisplayLayer
case object UpdateDisplayRamp
case object UpdateDisplayOpacity
case object UpdateDisplayBreaksCount
case object CollectMetadata
case class UpdateMetadata(md: Pot[Metadata] = Empty)

case class InitLMap(elemID: String, opts: LMapOptions)
case object UpdateTileLayer
case class UpdateZoomLevel(z: Option[Int])
