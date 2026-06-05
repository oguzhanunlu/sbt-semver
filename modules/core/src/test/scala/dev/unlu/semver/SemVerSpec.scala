package dev.unlu.semver

class SemVerSpec extends munit.FunSuite {

  // ----- of / unsafe -----

  test("of: accepts non-negative components") {
    assertEquals(SemVer.of(1, 2, 3).map(_.render), Right("1.2.3"))
    assertEquals(SemVer.of(0, 0, 0).map(_.render), Right("0.0.0"))
  }

  test("of: rejects negative major") {
    assert(SemVer.of(-1, 0, 0).isLeft)
  }

  test("of: rejects negative minor") {
    assert(SemVer.of(0, -1, 0).isLeft)
  }

  test("of: rejects negative patch") {
    assert(SemVer.of(0, 0, -1).isLeft)
  }

  test("of: accepts well-formed pre-release and build") {
    assertEquals(
      SemVer.of(1, 0, 0, Some("rc.1"), Some("sha.abc")).map(_.render),
      Right("1.0.0-rc.1+sha.abc")
    )
  }

  test("of: rejects pre-release with leading-zero numeric identifier") {
    assert(SemVer.of(1, 0, 0, preRelease = Some("01")).isLeft)
    assert(SemVer.of(1, 0, 0, preRelease = Some("rc.01")).isLeft)
  }

  test("of: accepts build with leading zeros") {
    assert(SemVer.of(1, 0, 0, build = Some("001")).isRight)
    assert(SemVer.of(1, 0, 0, build = Some("001.alpha")).isRight)
  }

  test("of: rejects empty pre-release") {
    assert(SemVer.of(1, 0, 0, preRelease = Some("")).isLeft)
  }

  test("of: rejects empty build") {
    assert(SemVer.of(1, 0, 0, build = Some("")).isLeft)
  }

  test("unsafe: throws on negative") {
    intercept[IllegalArgumentException](SemVer.unsafe(-1, 0, 0))
  }

  // ----- of(String) / unsafe(String) -----

  test("of: basic version") {
    val v = SemVer.of("1.2.3").toOption.get
    assertEquals(v.major, 1)
    assertEquals(v.minor, 2)
    assertEquals(v.patch, 3)
    assertEquals(v.preRelease, None)
    assertEquals(v.build, None)
  }

  test("of: with pre-release and build") {
    val v = SemVer.of("1.0.0-rc.1+sha.abc").toOption.get
    assertEquals(v.preRelease, Some("rc.1"))
    assertEquals(v.build, Some("sha.abc"))
    assertEquals(v.render, "1.0.0-rc.1+sha.abc")
  }

  test("of: leading-zero pre-release rejected") {
    assert(SemVer.of("1.0.0-01").isLeft)
  }

  test("of: malformed input rejected") {
    assert(SemVer.of("not-a-version").isLeft)
    assert(SemVer.of("1.2").isLeft)
    assert(SemVer.of("").isLeft)
  }

  test("of: rejects component that overflows Int") {
    assert(SemVer.of("99999999999999999999.0.0").isLeft)
    assert(SemVer.of("0.99999999999999999999.0").isLeft)
    assert(SemVer.of("0.0.99999999999999999999").isLeft)
  }

  test("of: rejects leading zeros in major/minor/patch") {
    assert(SemVer.of("01.0.0").isLeft)
    assert(SemVer.of("0.01.0").isLeft)
    assert(SemVer.of("0.0.01").isLeft)
  }

  test("unsafe: round-trips") {
    val s = "2.0.0-rc.1+sha.abc"
    assertEquals(SemVer.unsafe(s).render, s)
  }

  test("unsafe: throws on invalid") {
    intercept[IllegalArgumentException](SemVer.unsafe("not-a-version"))
    intercept[IllegalArgumentException](SemVer.unsafe("1.0.0-01"))
  }

  // ----- next* helpers -----

  test("nextMajor: bumps major, resets minor/patch, clears pre-release and build") {
    val v = SemVer.unsafe(1, 2, 3, Some("rc.1"), Some("sha.abc"))
    assertEquals(v.nextMajor, SemVer.unsafe(2, 0, 0))
  }

  test("nextMinor: bumps minor, resets patch, clears pre-release and build") {
    val v = SemVer.unsafe(1, 2, 3, Some("rc.1"), Some("sha.abc"))
    assertEquals(v.nextMinor, SemVer.unsafe(1, 3, 0))
  }

  test("nextPatch: bumps patch, clears pre-release and build") {
    val v = SemVer.unsafe(1, 2, 3, Some("rc.1"), Some("sha.abc"))
    assertEquals(v.nextPatch, SemVer.unsafe(1, 2, 4))
  }

  test("next*: chainable") {
    assertEquals(SemVer.unsafe(1, 0, 0).nextMajor.nextMajor, SemVer.unsafe(3, 0, 0))
  }

  test("next*: throw ArithmeticException on Int overflow") {
    intercept[ArithmeticException](SemVer.unsafe(Int.MaxValue, 0, 0).nextMajor)
    intercept[ArithmeticException](SemVer.unsafe(0, Int.MaxValue, 0).nextMinor)
    intercept[ArithmeticException](SemVer.unsafe(0, 0, Int.MaxValue).nextPatch)
  }

  // ----- render -----

  test("render: basic") {
    assertEquals(SemVer.unsafe(1, 2, 3).render, "1.2.3")
  }

  test("render: with pre-release only") {
    assertEquals(SemVer.unsafe(1, 0, 0, preRelease = Some("rc.1")).render, "1.0.0-rc.1")
  }

  test("render: with build only") {
    assertEquals(SemVer.unsafe(1, 0, 0, build = Some("sha.abc")).render, "1.0.0+sha.abc")
  }

  // ----- edge cases -----

  test("of: trailing dot in pre-release rejected") {
    assert(SemVer.of("1.0.0-rc.").isLeft)
  }

  test("of: trailing dot in build rejected") {
    assert(SemVer.of("1.0.0+001.").isLeft)
  }

  test("of: empty identifier in middle of pre-release rejected") {
    assert(SemVer.of("1.0.0-rc..beta").isLeft)
  }

  test("of: leading zero in non-first pre-release identifier rejected") {
    assert(SemVer.of("1.0.0-rc.01.beta").isLeft)
  }

  test("of: Int.MaxValue boundary accepted") {
    assertEquals(SemVer.of("2147483647.0.0").map(_.major), Right(Int.MaxValue))
  }

  test("of: one past Int.MaxValue rejected") {
    assert(SemVer.of("2147483648.0.0").isLeft)
  }

  test("of: case sensitivity preserved") {
    val a = SemVer.unsafe("1.0.0-RC.1")
    val b = SemVer.unsafe("1.0.0-rc.1")
    assert(a != b)
    assertEquals(a.preRelease, Some("RC.1"))
    assertEquals(b.preRelease, Some("rc.1"))
  }

  test("of: hyphen-only pre-release identifier accepted") {
    // Per BNF, "-" is a non-digit; "-" alone is a valid alphanumeric identifier.
    assert(SemVer.of("1.0.0--").isRight)
    assert(SemVer.of("1.0.0---").isRight)
  }

  test("of: mixed numeric and alphanumeric pre-release identifiers") {
    assert(SemVer.of("1.0.0-rc.1.alpha-beta.2").isRight)
  }

  test("of: non-ASCII characters in identifier rejected") {
    assert(SemVer.of("1.0.0-α").isLeft)
    assert(SemVer.of("1.0.0+α").isLeft)
  }

  test("of: whitespace rejected") {
    assert(SemVer.of(" 1.0.0").isLeft)
    assert(SemVer.of("1.0.0 ").isLeft)
    assert(SemVer.of("1.0.0-rc 1").isLeft)
  }

  test("of: empty identifier in middle of build rejected") {
    assert(SemVer.of("1.0.0+001..alpha").isLeft)
  }

  test("roundtrip: of → render preserves input") {
    val inputs = List(
      "0.0.0",
      "1.2.3",
      "10.20.30",
      "1.0.0-0",
      "1.0.0-rc.1",
      "1.0.0-alpha.beta",
      "1.0.0-rc-1",
      "1.0.0+sha.abc",
      "1.0.0+001",
      "1.0.0-rc.1+sha.abc"
    )
    inputs.foreach { s =>
      assertEquals(SemVer.of(s).map(_.render), Right(s), s"roundtrip failed for: $s")
    }
  }
}
