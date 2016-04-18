package geotrellis.admin.shared

import io.circe._
import io.circe.generic.semiauto._

object Implicits {

  implicit val decodeClassBreaks: Decoder[ClassBreaks] =
      Decoder.forProduct1("breaks")(ClassBreaks.apply)
  implicit val encodeClassBreaks: Encoder[ClassBreaks] =
      Encoder.forProduct1("breaks")(l => (l.breaks))

  implicit val decodeLayerDescription: Decoder[LayerDescription] =
      Decoder.forProduct2("name", "availableZooms")(LayerDescription.apply)
  implicit val encodeLayerDescription: Encoder[LayerDescription] =
      Encoder.forProduct2("name", "availableZooms")(l => (l.name, l.availableZooms))

}
