package dev.unlu.semver.circe

import dev.unlu.semver.SemVer

import _root_.io.circe.{Decoder, Encoder}

trait SemVerInstances {
  implicit val semverEncoder: Encoder[SemVer] = Encoder.encodeString.contramap(_.render)
  implicit val semverDecoder: Decoder[SemVer] = Decoder.decodeString.emap(SemVer.of)
}

object instances extends SemVerInstances
