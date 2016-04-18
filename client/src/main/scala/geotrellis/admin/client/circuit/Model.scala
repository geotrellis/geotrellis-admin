package geotrellis.admin.client.circuit

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._

import geotrellis.admin.shared._
import geotrellis.admin.shared.Implicits._

// Define models
case class RootModel(layerM: LayerModel = LayerModel(),
                     colorM: ColorRampModel = ColorRampModel(),
                     breaksM: ColorBreaksModel = ColorBreaksModel()
                   )
case class LayerModel(layers: Pot[Array[LayerDescription]] = Empty, selection: Option[LayerDescription] = None)
case class ColorRampModel(selection: Option[String] = None)
case class ColorBreaksModel(breaks: Pot[Array[Double]] = Empty, breakCount: Option[Int] = None)

// Define actions
sealed trait LayerActions
case object RefreshLayers extends LayerActions
case class UpdateLayers(layers: Array[LayerDescription]) extends LayerActions
case class SelectLayer(layer: Option[LayerDescription]) extends LayerActions
case object DeselectLayer extends LayerActions

sealed trait ColorRampActions
case class SelectColorRamp(ramp: Option[String]) extends ColorRampActions
case object DeselectColorRamp extends ColorRampActions

sealed trait ColorBreakActions
case object RefreshBreaks extends ColorBreakActions
case class SelectBreakCount(break: Option[Int]) extends ColorBreakActions
case class UpdateBreaks(breaks: Array[Double]) extends ColorBreakActions


