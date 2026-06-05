# sbt-semver

SemVer for Scala. BNF-modeled, zero deps, sbt plugin included.

```scala
import dev.unlu.semver.SemVer

// From a string
SemVer.of("1.2.3")
// res0: Either[String, SemVer] = Right(SemVer(1, 2, 3, None, None))
SemVer.of("not-a-version")
// res1: Either[String, SemVer] = Left("invalid SemVer string: not-a-version")
SemVer.unsafe("1.2.3")
// res2: SemVer = SemVer(1, 2, 3, None, None)

// From integers
SemVer.of(1, 2, 3)
// res3: Either[String, SemVer] = Right(SemVer(1, 2, 3, None, None))
SemVer.of(-1, 0, 0)
// res4: Either[String, SemVer] = Left("major must be >= 0, got -1")
SemVer.unsafe(1, 2, 3)
// res5: SemVer = SemVer(1, 2, 3, None, None)
SemVer.of(1, 0, 0, preRelease = Some("rc.1"))
// res6: Either[String, SemVer] = Right(SemVer(1, 0, 0, Some("rc.1"), None))
SemVer.of(1, 0, 0, preRelease = Some("rc.1"), build = Some("sha.abc"))
// res7: Either[String, SemVer] = Right(
//   SemVer(1, 0, 0, Some("rc.1"), Some("sha.abc"))
// )
SemVer.unsafe(1, 0, 0, build = Some("sha.abc"))
// res8: SemVer = SemVer(1, 0, 0, None, Some("sha.abc"))

// Inspect
val v = SemVer.unsafe("2.0.0-rc.1+sha.abc")
// v: SemVer = SemVer(2, 0, 0, Some("rc.1"), Some("sha.abc"))
v.major
// res9: Int = 2
v.preRelease
// res10: Option[String] = Some("rc.1")
v.render
// res11: String = "2.0.0-rc.1+sha.abc"

// Bump (pre-release and build are dropped)
val current = SemVer.unsafe("1.2.3-rc.1+sha.abc")
// current: SemVer = SemVer(1, 2, 3, Some("rc.1"), Some("sha.abc"))
current.nextMajor
// res12: SemVer = SemVer(2, 0, 0, None, None)
current.nextMinor
// res13: SemVer = SemVer(1, 3, 0, None, None)
current.nextPatch
// res14: SemVer = SemVer(1, 2, 4, None, None)

// Ordering (per spec §11; build metadata excluded from precedence)
SemVer.unsafe("1.0.0-alpha") < SemVer.unsafe("1.0.0")
// res15: Boolean = true
SemVer.unsafe("1.0.0-2") < SemVer.unsafe("1.0.0-11")
// res16: Boolean = true
List(
  SemVer.unsafe("1.0.0"),
  SemVer.unsafe("1.0.0-rc.1"),
  SemVer.unsafe("1.0.0-alpha"),
  SemVer.unsafe("1.0.0-alpha.1"),
  SemVer.unsafe("1.0.0-beta.2")
).sorted
// res17: List[SemVer] = List(
//   SemVer(1, 0, 0, Some("alpha"), None),
//   SemVer(1, 0, 0, Some("alpha.1"), None),
//   SemVer(1, 0, 0, Some("beta.2"), None),
//   SemVer(1, 0, 0, Some("rc.1"), None),
//   SemVer(1, 0, 0, None, None)
// )
```

All fields are validated against the [BNF grammar](https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions) at construction. For example, `1.0.0-01` is rejected because the spec forbids leading zeros in pre-release numeric identifiers. Build digits don't have that restriction, so `1.0.0+001` parses fine.

## Modules

| Artifact | Purpose | Targets |
|---|---|---|
| `dev.unlu:semver-core` | `SemVer` case class + parser. Zero dependencies. | Scala 2.12, 2.13, 3.3 |
| `dev.unlu:sbt-semver` | AutoPlugin that re-exports `SemVer` for use in `build.sbt`. | sbt 1.x |

## License

Apache-2.0. See [LICENSE](LICENSE).
