package geotrellis.admin.server.util

import geotrellis.raster._

object ErrorTile {
  val bytes: Array[Byte] = IntArrayTile.empty(1, 1).renderPng().bytes
}
