package geotrellis.admin.server

import akka.actor.Props
import akka.io.IO
import akka.actor.ActorSystem
import spray.can.Http

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("geotrellis-admin-server")
    val host = sys.env("GT_HOSTNAME")
    val port = sys.env("GT_PORT").toInt
    val service = system.actorOf(Props(classOf[GeotrellisAdminServiceActor]), "geotrellis-admin-service")

    IO(Http) ! Http.Bind(service, host, port)
  }
}
