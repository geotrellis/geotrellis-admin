package geotrellis.admin.client.circuit

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._

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
case class BreaksModel(breaks: Pot[Array[Double]] = Empty, breaksCount: Option[Int] = None)

case class LeafletModel(url: Option[String] = None, zoom: Option[Int] = None)
case class DisplayModel(
  layer: Option[LayerDescription] = None,
  ramp: Option[String] = None,
  opacity: Option[Int] = None,
  breaksCount: Option[Int] = None,
  metadata: Pot[Metadata] = Empty,
  leafletM: LeafletModel = LeafletModel()
)

// Define actions
sealed trait LayerActions
case object RefreshLayers extends LayerActions
case class UpdateLayers(layers: Pot[Array[LayerDescription]]) extends LayerActions
case class SelectLayer(layer: Option[LayerDescription]) extends LayerActions
case object DeselectLayer extends LayerActions

sealed trait ColorActions
case class SelectColorRamp(ramp: Option[String]) extends ColorActions
case class SetOpacity(opacity: Int) extends ColorActions

sealed trait BreaksActions
case object RefreshBreaks extends BreaksActions
case class SelectBreaksCount(breaks: Option[Int]) extends BreaksActions
case class UpdateBreaks(breaks: Pot[Array[Double]]) extends BreaksActions

sealed trait DisplayActions
case object UpdateDisplay extends DisplayActions
case object UpdateDisplayLayer extends DisplayActions
case object UpdateDisplayRamp extends DisplayActions
case object UpdateDisplayOpacity extends DisplayActions
case object UpdateDisplayBreaksCount extends DisplayActions
case object CollectMetadata extends DisplayActions
case class UpdateMetadata(md: Pot[Metadata]) extends DisplayActions

sealed trait LeafletActions
case object UpdateTileLayer extends LeafletActions
case class UpdateZoomLevel(z: Option[Int]) extends LeafletActions
