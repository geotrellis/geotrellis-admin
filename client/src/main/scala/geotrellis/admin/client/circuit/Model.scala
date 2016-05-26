package geotrellis.admin.client.circuit

import io.circe.Json
import scala.scalajs.js

import diode.data._
import geotrellis.admin.client._
import geotrellis.admin.client.facades._
import geotrellis.admin.shared._
//import io.circe.generic.semiauto._

/* The Models, i.e. our global Diode state */
case class RootModel(
  layerM: LayerModel = LayerModel(),
  colorM: ColorModel = ColorModel(),
  breaksM: BreaksModel = BreaksModel(),
  displayM: DisplayModel = DisplayModel()
)

case class LayerModel(
  layers: Pot[Array[LayerDescription]] = Empty,
  selection: Option[LayerDescription] = None
)

case class ColorModel(ramp: Option[String] = None, opacity: Int = 100)

case class BreaksModel(
  breaks: Pot[String] = Empty,
  breaksCount: Option[Int] = None
)

case class LeafletModel(
  zoom: Option[Int] = None,
  lmap: Option[LMap] = None,
  gtLayer: Option[LTileLayer] = None
) {
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
  rawMetadata: Pot[String] = Empty,
  attributes: Pot[ExtraAttrs] = Empty,
  leafletM: LeafletModel = LeafletModel()
)

// TODO Move this to where it should be.
case class ExtraAttrs(attrs: Map[String, Json])

/* Diode Actions */
case class SelectLayer(layer: Option[LayerDescription])
case class UpdateLayers(layers: Pot[Array[LayerDescription]] = Empty)
case object DeselectLayer
case object RefreshLayers

case class SelectColorRamp(ramp: Option[String])
case class SetOpacity(opacity: Int)

case class SelectBreaksCount(breaks: Option[Int])
case class UpdateBreaks(breaks: Pot[String] = Empty)
case object RefreshBreaks

/* Metadata */
case class UpdateMetadata(md: Pot[Metadata] = Empty, json: Pot[String] = Empty)
case object CollectMetadata

/* Extra Attributes */
case class UpdateAttributes(attrs: Pot[ExtraAttrs] = Empty)
case object CollectAttributes

/* Display Actions */
case object UpdateDisplay
case object UpdateDisplayBreaksCount
case object UpdateDisplayLayer
case object UpdateDisplayOpacity
case object UpdateDisplayRamp

case class InitLMap(elemID: String, opts: LMapOptions)
case class UpdateZoomLevel(z: Option[Int])
case object UpdateTileLayer
