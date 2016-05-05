package geotrellis.admin.server.util

import geotrellis.raster._
import geotrellis.raster.render._

object ErrorTile {
  private val png = IntArrayTile.empty(1, 1).renderPng()

  def apply(): Array[Byte] = png.bytes
}


