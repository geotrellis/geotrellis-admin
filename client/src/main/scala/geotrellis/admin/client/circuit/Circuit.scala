package geotrellis.admin.client.circuit

import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import io.circe._
import io.circe.scalajs._
import io.circe.generic.semiauto._
import org.scalajs.dom.ext.Ajax
import cats.data.Xor

import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext.Implicits.global

import geotrellis.admin.shared._
import Catalog._

/** Display actions */
class DisplayHandler[M](modelRW: ModelRW[M, DisplayModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case UpdateDisplay => {
      effectOnly(
        Effect.action(UpdateDisplayLayer) + Effect.action(UpdateDisplayBreaksCount) + Effect.action(UpdateDisplayRamp) +
        Effect.action(UpdateDisplayOpacity) >>
        Effect.action(RefreshBreaks) >>
        Effect.action(UpdateTileLayer) + Effect.action(CollectMetadata)
      )}
    case UpdateDisplayLayer => {
      val ld = AppCircuit.zoom(_.layerM.selection).value
      updated(
        value.copy(layer = ld),
        Effect.action(UpdateZoomLevel(ld.flatMap(_.availableZooms.headOption)))
      )
    }
    case UpdateDisplayRamp =>
      updated(value.copy(ramp = AppCircuit.zoom(_.colorM.ramp).value))
    case UpdateDisplayOpacity =>
      updated(value.copy(opacity = Some(AppCircuit.zoom(_.colorM.opacity).value)))
    case UpdateDisplayBreaksCount =>
      updated(value.copy(breaksCount = AppCircuit.zoom(_.breaksM.breaksCount).value))
    case CollectMetadata => {
      effectOnly(Effect(Catalog.metadata(currentLayerName.value.get, currentZoomLevel.value.get).map { res =>
        val parsed = JSON.parse(res.responseText)
        val md = decodeJs[Metadata](parsed)
        md match {
          case Xor.Right(md) => UpdateMetadata(Ready(md))
          case Xor.Left(e) => UpdateMetadata(Failed(e))
        }
      }))
    }
    case UpdateMetadata(md) =>
      updated(value.copy(metadata = md))
  }
}

/** Leaflet handler */
class LeafletHandler[M](modelRW: ModelRW[M, LeafletModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case UpdateTileLayer => {
      val urlTemplate = for {
        layerName <- currentLayerName.value
        colorRamp <- currentColorRamp.value
        breaks <- currentBreaks.value.toOption
      } yield s"""/gt/tms/${layerName}/{z}/{x}/{y}?colorRamp=${colorRamp}&breaks=${breaks.mkString(",")}"""

      updated(value.copy(url = urlTemplate))
    }
    case UpdateZoomLevel(zl) =>{
      updated(value.copy(zoom = zl), Effect.action(CollectMetadata))
    }
  }
}

/** Layer actions */
class LayerHandler[M](modelRW: ModelRW[M, LayerModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshLayers =>
      effectOnly(Effect(Catalog.list.map { res =>
        val parsed = JSON.parse(res.responseText)
        val layers = decodeJs[Array[LayerDescription]](parsed)
        layers match {
          case Xor.Right(ls) => UpdateLayers(Ready(ls))
          case Xor.Left(e) => UpdateLayers(Failed(e))
        }
      }))
    case UpdateLayers(layers) =>
      updated(value.copy(layers = layers))
    case SelectLayer(layer) =>
      updated(value.copy(selection = layer))
    case DeselectLayer =>
      updated(value.copy(selection = None))
  }
}

/** Breaks actions */
class BreaksHandler[M](modelRW: ModelRW[M, BreaksModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshBreaks =>
      effectOnly(Effect(
        Catalog.breaks(currentLayerName.value.getOrElse(""), currentBreaksCount.value.getOrElse(0)).map { res =>
          val parsed = JSON.parse(res.responseText)
          val breaks = decodeJs[ClassBreaks](parsed)
          breaks match {
            case Xor.Right(brs) => UpdateBreaks(Ready(brs.classBreaks))
            case Xor.Left(e) => UpdateBreaks(Failed(e))
          }
        }
      ))
    case UpdateBreaks(breaks) => {
      updated(value.copy(breaks = breaks), Effect.action(UpdateTileLayer))
    }
    case SelectBreaksCount(count) => {
      updated(value.copy(breaksCount = count))
    }
  }
}

/** Color actions */
class ColorHandler[M](modelRW: ModelRW[M, ColorModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case SelectColorRamp(ramp) =>
      updated(value.copy(ramp = ramp))
    case SetOpacity(opacity) =>
      updated(value.copy(opacity = opacity))
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // provides initial model to the Circuit
  override def initialModel = RootModel()
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new DisplayHandler(zoomRW(_.displayM)((root, newVal) => root.copy(displayM = newVal))),
    new LeafletHandler(
      zoomRW(_.displayM)((root, newVal) => root.copy(displayM = newVal))
        .zoomRW(_.leafletM)((display, newVal) => display.copy(leafletM = newVal))
    ),
    new LayerHandler(zoomRW(_.layerM)((root, newVal) => root.copy(layerM = newVal))),
    new ColorHandler(zoomRW(_.colorM)((root, newVal) => root.copy(colorM = newVal))),
    new BreaksHandler(zoomRW(_.breaksM)((root, newVal) => root.copy(breaksM = newVal)))
  )
}
