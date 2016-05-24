package geotrellis.admin.client

import scala.scalajs.LinkingInfo

object SiteConfig {
  def adminHostUrl(uri: String) =
    if (LinkingInfo.developmentMode) s"http://localhost:8080${uri}" else uri
}


