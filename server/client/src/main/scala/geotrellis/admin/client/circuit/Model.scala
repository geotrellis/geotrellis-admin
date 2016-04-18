package geotrellis.admin.client.circuit

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector

import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._

// TODO remove this garbage
case class Layer(name: String, zooms: Array[Int])

object Layer {
  implicit val decodeLayer: Decoder[Layer] =
      Decoder.forProduct2("name", "availableZooms")(Layer.apply)
}

// Define models
case class RootModel(layerM: LayerModel = LayerModel(), colorM: ColorRampModel = ColorRampModel())
case class LayerModel(layers: Pot[Array[Layer]] = Empty, selection: Option[Layer] = None)
case class ColorRampModel(ramps: Pot[Array[String]] = Empty, selection: Option[String] = None)

// Define actions
sealed trait LayerActions
case object InitLayers extends LayerActions
case class UpdateLayers(layers: Array[Layer]) extends LayerActions
case class SelectLayer(layer: Layer) extends LayerActions
case object DeselectLayer extends LayerActions

sealed trait ColorRampActions
case class SelectColorRamp(ramp: String) extends ColorRampActions
case object DeselectColorRamp extends ColorRampActions
