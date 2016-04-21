package geotrellis.admin.client.circuit

import org.scalajs.dom.ext.Ajax

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._

import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext.Implicits.global

import geotrellis.admin.shared._
import geotrellis.admin.shared.Implicits._

import Catalog._

// Define handlers
class LayerHandler[M](modelRW: ModelRW[M, LayerModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshLayers =>
      effectOnly(Effect(Catalog.list.map { res =>
        val parsed = JSON.parse(res.responseText)
        val layers = decodeJs[Array[LayerDescription]](parsed)
        UpdateLayers(layers.getOrElse(Array[LayerDescription]()))
      }))
    case UpdateLayers(layers) =>
      updated(value.copy(layers = Ready(layers)))
    case SelectLayer(layer) =>
      println(layerNameReader.value, breakCountReader.value, colorRampReader.value)
      updated(value.copy(selection = layer))
    case DeselectLayer =>
      updated(value.copy(selection = None))
  }
}
class ColorBreaksHandler[M](modelRW: ModelRW[M, ColorBreaksModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshBreaks =>
      effectOnly(Effect(Catalog.breaks.map { res =>
        val parsed = JSON.parse(res.responseText)
        val layers = decodeJs[Array[Double]](parsed)
        println(parsed)
        UpdateBreaks(layers.getOrElse(Array()))
      }))
    case UpdateBreaks(breaks) =>
      updated(value.copy(breaks = Ready(breaks)))
    case SelectBreakCount(count) => {
      updated(value.copy(breakCount = count), Effect.action(RefreshBreaks))
    }
  }
}

class ColorRampHandler[M](modelRW: ModelRW[M, ColorRampModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case SelectColorRamp(ramp) =>
      updated(value.copy(selection = ramp))
    case DeselectColorRamp =>
      updated(value.copy(selection = None))
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // provides initial model to the Circuit
  override def initialModel = RootModel()
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new LayerHandler(zoomRW(_.layerM)((root, newVal) => root.copy(layerM = newVal))),
    new ColorRampHandler(zoomRW(_.colorM)((root, newVal) => root.copy(colorM = newVal))),
    new ColorBreaksHandler(zoomRW(_.breaksM)((root, newVal) => root.copy(breaksM = newVal)))
  )
}
