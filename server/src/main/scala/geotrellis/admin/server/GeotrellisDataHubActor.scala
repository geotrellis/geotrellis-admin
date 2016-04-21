package geotrellis.admin.server

import com.typesafe.config.Config

import geotrellis.ingest.test._

class GeotrellisDataHubActor(config: Config) extends GeotrellisIngestTestServiceActor(config) {

  override def serviceRoute = get {
    pathPrefix("gt") {
      pathPrefix("catalog")(meta) ~
      pathPrefix("meta")(meta) ~
      pathPrefix("tms")(tms) ~
      pathPrefix("breaks")(breaks)
    } ~
    pathEndOrSingleSlash {
      getFromResource("geotrellis/viewer/index.html")
    } ~
    pathPrefix("") {
      getFromResourceDirectory("geotrellis/viewer")
    }
  }

}

