package geotrellis.admin.client.circuit

import org.scalajs.dom.ext.Ajax

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import io.circe.scalajs._
import io.circe._

import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._

import scala.scalajs.js.JSON

import scala.concurrent.ExecutionContext.Implicits.global

// Define handlers
class LayerHandler[M](modelRW: ModelRW[M, LayerModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case InitLayers =>
      effectOnly(Effect(Catalog.list.map { res =>
        val parsed = JSON.parse(res.responseText)
        val layers = decodeJs[Array[Layer]](parsed)
        UpdateLayers(layers.getOrElse(Array[Layer]()))
      }))
      //.onSuccess { case xhr => println(xhrresponseText) }
      //effectOnly(Effect(AjaxClient[Api].getTodos().call().map(UpdateAllTodos)))
      //updated(LayerModel(Ready(Seq(Layer("test", Vector(1))))))
    case UpdateLayers(layers) =>
      updated(value.copy(layers = Ready(layers)))
    case SelectLayer(layer) =>
      // got new todos, update model
      updated(value.copy(selection = Some(layer)))
    case DeselectLayer =>
      updated(value.copy(selection = None))
  }
}

class ColorRampHandler[M](modelRW: ModelRW[M, ColorRampModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case SelectColorRamp(ramp) =>
      // got new todos, update model
      updated(value.copy(selection = Some(ramp)))
    case DeselectColorRamp =>
      updated(value.copy(selection = None))
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // provides initial model to the Circuit
  override def initialModel = RootModel()
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new LayerHandler(zoomRW(_.layerM)((m, v) => m.copy(layerM = v))),
    new ColorRampHandler(zoomRW(_.colorM)((m, v) => m.copy(colorM = v)))
  )
}
