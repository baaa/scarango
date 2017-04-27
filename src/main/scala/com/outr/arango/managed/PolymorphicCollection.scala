package com.outr.arango.managed

import com.outr.arango.{DocumentOption, Macros}
import com.outr.arango.rest.CreateInfo
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.language.experimental.macros

class PolymorphicCollection[T <: PolymorphicDocumentOption]
                                    (val graph: Graph, name: String, val types: List[PolymorphicType[T]] = Nil)
                                    extends Collection[T](graph, name) {
  def withType[P <: T](value: String): PolymorphicCollection[T] = macro Macros.withType[P]
  private lazy val typeMap: Map[String, PolymorphicType[T]] = types.map(t => t.value -> t).toMap
  override protected implicit val encoder: Encoder[T] = new Encoder[T] {
    override def apply(a: T): Json = typeMap(a._type).encoder(a)
  }
  override protected implicit val decoder: Decoder[T] = new Decoder[T] {
    override def apply(c: HCursor): Result[T] = typeMap(c.field("_type").as[String].getOrElse(throw new RuntimeException(s"_type not found in polymorphic document"))).decoder(c)
  }

  override protected def updateDocument(document: T, info: CreateInfo): T = typeMap(document._type).updateDocument(document, info)
}

trait PolymorphicDocumentOption extends DocumentOption {
  def _type: String
}

case class PolymorphicType[T](value: String,
                              encoder: Encoder[T],
                              decoder: Decoder[T],
                              updateDocument: (T, CreateInfo) => T)