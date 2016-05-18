package geotrellis.admin.server.util

import geotrellis.raster._
import geotrellis.raster.render._

object ErrorTile {
  val bytes = IntArrayTile.empty(1, 1).renderPng().bytes
}
