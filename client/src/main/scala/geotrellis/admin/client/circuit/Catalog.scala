package geotrellis.admin.client.circuit

import diode._
import diode.data._
import geotrellis.admin.client._
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import scala.concurrent.Future

object Catalog {
  // TODO Check how these `Option` values are used, and see if `zoomMap` is better
  val currentLayerName: ModelR[RootModel, Option[String]] = ClientCircuit.zoomMap(_.displayM.layer)(_.name)
  val currentColorRamp: ModelR[RootModel, Option[String]] = ClientCircuit.zoom(_.displayM.ramp)
  val currentBreaksCount: ModelR[RootModel, Option[Int]] = ClientCircuit.zoom(_.displayM.breaksCount)
  val currentBreaks: ModelR[RootModel, Pot[String]] = ClientCircuit.zoom(_.breaksM.breaks)
  val currentOpacity: ModelR[RootModel, Option[Int]] = ClientCircuit.zoom(_.displayM.opacity)
  val currentZoomLevel: ModelR[RootModel, Option[Int]] = ClientCircuit.zoom(_.displayM.leafletM.zoom)

  def list: Future[XMLHttpRequest] =
    Ajax.get(SiteConfig.adminHostUrl("/gt/layers"))

  def metadata(name: String, zoom: Int): Future[XMLHttpRequest] =
    Ajax.get(SiteConfig.adminHostUrl(s"/gt/metadata/${name}/${zoom}"))

  def attributes(name: String, zoom: Int): Future[XMLHttpRequest] = {
    println("Calling gt/attributes...")

    Ajax.get(SiteConfig.adminHostUrl(s"/gt/attributes/${name}/${zoom}"))
  }

  def bounds(name: String, zoom: Int): Future[XMLHttpRequest] =
    Ajax.get(SiteConfig.adminHostUrl(s"/gt/bounds/${name}/${zoom}"))

  def breaks(name: String, breaks: Int): Future[XMLHttpRequest] =
    Ajax.get(SiteConfig.adminHostUrl(s"/gt/breaks/${name}/${breaks}"))
}
