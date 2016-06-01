package geotrellis.admin.client.circuit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSON
import scala.scalajs.js

import Catalog._
import cats.data.Xor
import diode._
import diode.data._
import diode.react.ReactConnector
import geotrellis.admin.client._
import geotrellis.admin.client.facades._
import geotrellis.admin.shared._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.scalajs._
import scala.util.Try

/** Display actions */
class DisplayHandler[M](modelRW: ModelRW[M, DisplayModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case UpdateDisplay =>
      effectOnly(
        Effect.action(UpdateDisplayLayer) + Effect.action(UpdateDisplayBreaksCount) +  Effect.action(UpdateDisplayRamp) + Effect.action(UpdateDisplayOpacity) >>
        Effect.action(RefreshBreaks)
      )
    case UpdateDisplayLayer =>
      updated(value.copy(layer = ClientCircuit.zoom(_.layerM.selection).value))
    case UpdateDisplayRamp =>
      updated(value.copy(ramp = ClientCircuit.zoom(_.colorM.ramp).value))
    case UpdateDisplayOpacity =>
      updated(value.copy(opacity = Some(ClientCircuit.zoom(_.colorM.opacity).value)))
    case UpdateDisplayBreaksCount =>
      updated(value.copy(breaksCount = ClientCircuit.zoom(_.breaksM.breaksCount).value))
    case CollectMetadata => {
      effectOnly(Effect(Catalog.metadata(currentLayerName.value.get, currentZoomLevel.value.get).map { res =>
        val parsed: js.Any = JSON.parse(res.responseText)

        decodeJs[Metadata](parsed) match {
          case Xor.Right(m) => UpdateMetadata(Ready(m), Ready(res.responseText))
          case Xor.Left(e) => UpdateMetadata(Failed(e), Ready(res.responseText))
        }
      }))
    }
    case CollectAttributes => {
      effectOnly(Effect(Catalog.attributes(currentLayerName.value.get, currentZoomLevel.value.get).map { res =>
        val parsed: js.Any = JSON.parse(res.responseText)

        decodeJs[Map[String, Json]](parsed) match {
          case Xor.Right(a) => UpdateAttributes(Ready(ExtraAttrs(a)))
          case Xor.Left(e) => UpdateAttributes(Failed(e))
        }
      }))
    }
    case UpdateMetadata(md, json) => updated(value.copy(metadata = md, rawMetadata = json))
    case UpdateAttributes(attrs) => updated(value.copy(attributes = attrs))
  }
}

/** Leaflet handler */
class LeafletHandler[M](modelRW: ModelRW[M, LeafletModel]) extends ActionHandler(modelRW) {
  override def handle = {
    case InitLMap(elemID: String, mapOpts: LMapOptions) =>
      updated(value.copy(lmap = Try(Leaflet.map(elemID, mapOpts)).toOption))
    case UpdateTileLayer => {
      val displayModel = ClientCircuit.zoom(_.displayM).value
      val gtLayer = for {
        layer <- displayModel.layer
        colorRamp <- displayModel.ramp
        breaks <- currentBreaks.value.toOption
        opacity <- displayModel.opacity
        minZoom = layer.availableZooms.min
        maxZoom = layer.availableZooms.max
      } yield {
        value.lmap.foreach { lmap: LMap =>
          value.gtLayer.foreach(lmap.removeLayer(_))
        }
        value.gtLayer.foreach(value.lmap.get.removeLayer(_))
        val url = SiteConfig.adminHostUrl(s"""/gt/tms/${layer.name}/{z}/{x}/{y}?colorRamp=${colorRamp}&breaks=${breaks}&opacity=${opacity}""")
        val newLayer = LTileLayer(url, value.tileLayerOpts(minZoom, maxZoom))
        value.lmap.map { newLayer.addTo(_) }
        newLayer
      }

      /* Fix the "no initial metadata" bug (GH #27)*/
      val zoom: Option[Int] = value.zoom.orElse(value.lmap.map(_.getZoom))

      updated(value.copy(gtLayer = gtLayer), Effect.action(UpdateZoomLevel(zoom)))
    }
    case UpdateZoomLevel(zl) =>
      updated(
        value.copy(zoom = zl),
        Effect.action(CollectMetadata) + Effect.action(CollectAttributes)
      )
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

object ClientCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // provides initial model to the Circuit
  override def initialModel = RootModel()
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new LeafletHandler(
      zoomRW(_.displayM)((root, newVal) => root.copy(displayM = newVal))
        .zoomRW(_.leafletM)((display, newVal) => display.copy(leafletM = newVal))
    ),
    new DisplayHandler(zoomRW(_.displayM)((root, newVal) => root.copy(displayM = newVal))),
    new LayerHandler(zoomRW(_.layerM)((root, newVal) => root.copy(layerM = newVal))),
    new ColorHandler(zoomRW(_.colorM)((root, newVal) => root.copy(colorM = newVal))),
    new BreaksHandler(zoomRW(_.breaksM)((root, newVal) => root.copy(breaksM = newVal)))
  )
}
