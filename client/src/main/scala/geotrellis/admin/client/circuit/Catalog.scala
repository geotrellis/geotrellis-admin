package geotrellis.admin.client.circuit

import diode._
import diode.data._
import diode.util._
import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global

import geotrellis.admin.shared.LayerDescription

object Catalog {
  // Default to the empty string
  val layerNameReader: ModelR[RootModel, String] = AppCircuit.zoom(_.layerM.selection.getOrElse(LayerDescription()).name)

  // Default to 5 breaks
  val breakCountReader: ModelR[RootModel, Int] = AppCircuit.zoom(_.breaksM.breakCount.getOrElse(5))

  // Default to "blue-to-orange" breaks
  val colorRampReader: ModelR[RootModel, String] = AppCircuit.zoom(_.colorM.selection.getOrElse("blue-to-orange"))

  def list = Ajax.get("http://localhost:8088/catalog")
  def detail(num: Int) = Ajax.get(s"http://localhost:8088/catalog/${layerNameReader.value}")
  def breaks = Ajax.get(s"http://localhost:8088/catalog/${layerNameReader.value}/${breakCountReader.value}")
}
