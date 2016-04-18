package geotrellis.admin

package object shared {
  case class ClassBreaks(breaks: Array[Double] = Array())

  case class LayerDescription(name: String = "", availableZooms: Seq[Int] = Seq())
}
