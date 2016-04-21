package geotrellis.admin.server

import akka.actor.Props
import akka.io.IO
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import spray.can.Http

object Server {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("geotrellis-ingest-test")
    val config = ConfigFactory.load()
    val host = config.getString("geotrellis.hostname")
    val port = config.getInt("geotrellis.port")
    val service = system.actorOf(Props(classOf[GeotrellisDataHubActor], config), "geotrellis-ingest-test-service")

    IO(Http) ! Http.Bind(service, host, port)
  }
}
