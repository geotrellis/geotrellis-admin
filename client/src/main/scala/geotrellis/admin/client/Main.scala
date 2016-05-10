package geotrellis.admin.client

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import scalacss.Defaults._
import scalacss.ScalaCssReact._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.{UndefOr, undefined}

import geotrellis.admin.client.routes._
import geotrellis.admin.client.components._
import geotrellis.admin.client.circuit._

object Main extends JSApp {

  // apply app css
  AppCSS.load()

  // application location
  sealed trait Loc
  case object DashboardLoc extends Loc

  val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    def layout = AppCircuit.wrap({r: RootModel => r})(GeotrellisAdminViewer(_))//_.displayM)(GeotrellisAdminViewer(_))

    (staticRoute(root, DashboardLoc) ~> render(layout)).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))

  }

  /** The router is itself a React component, which at this point is not mounted (U-suffix) */
  val router: ReactComponentU[Unit, Resolution[Loc], Any, TopNode] =
    Router(baseUrl, routerConfig.logToConsole)()

  @JSExport
  def main(): Unit = {
    val mounted = ReactDOM.render(router, dom.document.getElementsByClassName("app")(0))
  }
}

