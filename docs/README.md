# sbt-semver

[![CI](https://github.com/oguzhanunlu/sbt-semver/actions/workflows/ci.yml/badge.svg)](https://github.com/oguzhanunlu/sbt-semver/actions/workflows/ci.yml)

SemVer for Scala. BNF-modeled, zero deps, sbt plugin included.

```scala mdoc
import dev.unlu.semver.SemVer

// From a string
SemVer.of("1.2.3")
SemVer.of("not-a-version")
SemVer.unsafe("1.2.3")

// From integers
SemVer.of(1, 2, 3)
SemVer.of(-1, 0, 0)
SemVer.unsafe(1, 2, 3)
SemVer.of(1, 0, 0, preRelease = Some("rc.1"))
SemVer.of(1, 0, 0, preRelease = Some("rc.1"), build = Some("sha.abc"))
SemVer.unsafe(1, 0, 0, build = Some("sha.abc"))

// Inspect
val v = SemVer.unsafe("2.0.0-rc.1+sha.abc")
v.major
v.preRelease
v.render

// Bump (pre-release and build are dropped)
val current = SemVer.unsafe("1.2.3-rc.1+sha.abc")
current.nextMajor
current.nextMinor
current.nextPatch

// Ordering (per spec §11; build metadata excluded from precedence)
SemVer.unsafe("1.0.0-alpha") < SemVer.unsafe("1.0.0")
SemVer.unsafe("1.0.0-2") < SemVer.unsafe("1.0.0-11")
List(
  SemVer.unsafe("1.0.0"),
  SemVer.unsafe("1.0.0-rc.1"),
  SemVer.unsafe("1.0.0-alpha"),
  SemVer.unsafe("1.0.0-alpha.1"),
  SemVer.unsafe("1.0.0-beta.2")
).sorted
```

All fields are validated against the [BNF grammar](https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions) at construction. For example, `1.0.0-01` is rejected because the spec forbids leading zeros in pre-release numeric identifiers. Build digits don't have that restriction, so `1.0.0+001` parses fine.

## Modules

| Artifact | Purpose | Targets |
|---|---|---|
| `dev.unlu:semver-core` | `SemVer` case class + parser. Zero dependencies. | Scala 2.12, 2.13, 3.3 |
| `dev.unlu:sbt-semver` | AutoPlugin that re-exports `SemVer` for use in `build.sbt`. | sbt 1.x |

## License

Apache-2.0. See [LICENSE](LICENSE).
