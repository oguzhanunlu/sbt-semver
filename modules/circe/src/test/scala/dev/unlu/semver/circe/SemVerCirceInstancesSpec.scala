package dev.unlu.semver.circe

import dev.unlu.semver.SemVer
import dev.unlu.semver.circe.instances._

import _root_.io.circe.Json
import _root_.io.circe.parser.decode
import _root_.io.circe.syntax._

class SemVerCirceInstancesSpec extends munit.FunSuite {

  test("Encoder: emits canonical string form") {
    assertEquals(SemVer.unsafe("1.2.3").asJson, Json.fromString("1.2.3"))
    assertEquals(SemVer.unsafe("2.0.0-rc.1+sha.abc").asJson, Json.fromString("2.0.0-rc.1+sha.abc"))
  }

  test("Decoder: parses canonical string form") {
    assertEquals(decode[SemVer]("\"1.2.3\""), Right(SemVer.unsafe("1.2.3")))
    assertEquals(decode[SemVer]("\"2.0.0-rc.1+sha.abc\""), Right(SemVer.unsafe("2.0.0-rc.1+sha.abc")))
  }

  test("Decoder: rejects malformed input") {
    assert(decode[SemVer]("\"not-a-version\"").isLeft)
  }

  test("Decoder: rejects non-string JSON") {
    assert(decode[SemVer]("123").isLeft)
    assert(decode[SemVer]("null").isLeft)
  }

  test("roundtrip: encode then decode preserves value") {
    val v = SemVer.unsafe("1.2.3-rc.1+sha.abc")
    assertEquals(decode[SemVer](v.asJson.noSpaces), Right(v))
  }
}
