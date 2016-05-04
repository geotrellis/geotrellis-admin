package geotrellis.admin.server

import akka.actor.Props
import akka.io.IO
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import spray.can.Http

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("geotrellis-admin-server")
    val config = ConfigFactory.load()
    val host = config.getString("geotrellis.hostname")
    val port = config.getInt("geotrellis.port")
    val service = system.actorOf(Props(classOf[GeotrellisAdminServiceActor], config), "geotrellis-admin-service")

    IO(Http) ! Http.Bind(service, host, port)
  }
}
