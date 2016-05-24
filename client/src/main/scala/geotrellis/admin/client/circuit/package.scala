package geotrellis.admin.client

import geotrellis.admin.shared._
import io.circe._

package object circuit {

  implicit val decodeClassBreaks: Decoder[ClassBreaks] =
    Decoder.forProduct1("classBreaks")(ClassBreaks.apply)
  implicit val encodeClassBreaks: Encoder[ClassBreaks] =
    Encoder.forProduct1("classBreaks")(l => (l.classBreaks))

  implicit val decodeLayerDescription: Decoder[LayerDescription] =
    Decoder.forProduct2("name", "availableZooms")(LayerDescription.apply)
  implicit val encodeLayerDescription: Encoder[LayerDescription] =
    Encoder.forProduct2("name", "availableZooms")(l => (l.name, l.availableZooms))

  implicit val decodeExtent: Decoder[Extent] =
    Decoder.forProduct4("xmin", "ymin", "xmax", "ymax")(Extent.apply)
  implicit val encodeExtent: Encoder[Extent] =
    Encoder.forProduct4("xmin", "ymin", "xmax", "ymax")(e => (e.xmin, e.ymin, e.xmax, e.ymax))

  implicit val decodeTileLayout: Decoder[TileLayout] =
    Decoder.forProduct4("layoutCols", "layoutRows", "tileCols", "tileRows")(TileLayout.apply)
  implicit val encodeTileLayout: Encoder[TileLayout] =
    Encoder.forProduct4("layoutCols", "layoutRows", "tileCols", "tileRows")(tl => (tl.layoutCols, tl.layoutRows, tl.tileCols, tl.tileRows))

  implicit val decodeLayoutDefinition: Decoder[LayoutDefinition] =
    Decoder.forProduct2("extent", "tileLayout")(LayoutDefinition.apply)
  implicit val encodeLayoutDefinition: Encoder[LayoutDefinition] =
    Encoder.forProduct2("extent", "tileLayout")(ld => (ld.extent, ld.tileLayout))

  implicit val decodeKey: Decoder[Key] =
    Decoder.forProduct2("col", "row")(Key.apply)
  implicit val encodeKey: Encoder[Key] =
    Encoder.forProduct2("col", "row")(b => (b.col, b.row))

  implicit val decodeBounds: Decoder[Bounds] =
    Decoder.forProduct2("minKey", "maxKey")(Bounds.apply)
  implicit val encodeBounds: Encoder[Bounds] =
    Encoder.forProduct2("minKey", "maxKey")(b => (b.minKey, b.maxKey))

  implicit val decodeMetadata: Decoder[Metadata] =
    Decoder.forProduct5("extent", "layoutDefinition", "bounds", "cellType", "crs")(Metadata.apply)
  implicit val encodeMetadata: Encoder[Metadata] =
    Encoder.forProduct5("extent", "layoutDefinition", "bounds", "cellType", "crs")(md =>
      (md.extent, md.layoutDefinition, md. bounds, md.cellType, md.crs)
    )
}
