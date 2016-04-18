package geotrellis.admin.server

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import com.twitter.finagle.http.filter.Cors._

import com.twitter.finagle.{http, Http, Service}
import com.twitter.finagle.param.Stats
import com.twitter.server.TwitterServer
import com.twitter.finagle.http._
import com.twitter.util._

import services._


object Main extends TwitterServer {

  val helloWorld = get("hello" :: paramOption("name")) { name: Option[String] => Ok(s"Hello, ${name.getOrElse("world")}!") }
  val api: Service[Request, Response] = new HttpFilter(UnsafePermissivePolicy).andThen(
                                         (helloWorld :+:
                                          Colors.route :+:
                                          Catalog.listRoute :+:
                                          Catalog.breaksRoute :+:
                                          Catalog.tileRoute :+:
                                          Catalog.metadataRoute
                                         ).toService
                                        )

  def main(): Unit = {
    val server = Http.server
      .configured(Stats(statsReceiver))
      .serve(":8088", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}

