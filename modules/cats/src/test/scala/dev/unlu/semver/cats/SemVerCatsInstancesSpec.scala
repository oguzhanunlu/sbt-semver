package dev.unlu.semver.cats

import _root_.cats.syntax.eq._
import _root_.cats.syntax.show._
import _root_.cats.{Eq, Order, Show}

import dev.unlu.semver.SemVer
import dev.unlu.semver.cats.instances._

class SemVerCatsInstancesSpec extends munit.FunSuite {

  test("Show: uses render") {
    assertEquals(SemVer.unsafe(1, 2, 3).show, "1.2.3")
    assertEquals(SemVer.unsafe("2.0.0-rc.1+sha.abc").show, "2.0.0-rc.1+sha.abc")
  }

  test("Eq: same values are equal, different values are not") {
    assert(SemVer.unsafe(1, 2, 3) === SemVer.unsafe(1, 2, 3))
    assert(SemVer.unsafe(1, 2, 3) =!= SemVer.unsafe(1, 2, 4))
  }

  test("Order: agrees with the underlying Ordering") {
    assert(SemVer.unsafe("1.0.0-alpha") < SemVer.unsafe("1.0.0"))
    assertEquals(Order[SemVer].compare(SemVer.unsafe(1, 2, 3), SemVer.unsafe(1, 2, 3)), 0)
  }
}
