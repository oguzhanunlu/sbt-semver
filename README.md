# sbt-semver

[![CI](https://github.com/oguzhanunlu/sbt-semver/actions/workflows/ci.yml/badge.svg)](https://github.com/oguzhanunlu/sbt-semver/actions/workflows/ci.yml)

SemVer for Scala. BNF-modeled, zero deps, sbt plugin included.

## Install

In `build.sbt`:

```scala
libraryDependencies += "dev.unlu" %% "semver-core" % "0.2.0"

// Optional: cats integration (Show, Eq, Order instances)
libraryDependencies += "dev.unlu" %% "semver-cats" % "0.2.0"

// Optional: circe JSON Encoder / Decoder
libraryDependencies += "dev.unlu" %% "semver-circe" % "0.2.0"
```

In `project/plugins.sbt`:

```scala
addSbtPlugin("dev.unlu" % "sbt-semver" % "0.2.0")
```

## Plugin usage

The plugin auto-imports `SemVer`, so it is available in `build.sbt` with no import:

```scala
// Validated at load time: a malformed version fails the build immediately
version := SemVer.unsafe("1.4.0-rc.1").render

// Branch build logic on versions
val akkaVersion = SemVer.unsafe("2.6.20")
libraryDependencies += {
  if (akkaVersion >= SemVer.unsafe("2.6.0")) "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion.render
  else "com.typesafe.akka" %% "akka-actor" % akkaVersion.render
}

// Compute versions in tasks
val printNextMinor = taskKey[Unit]("Print the next minor version")
printNextMinor := println(SemVer.unsafe(version.value).nextMinor.render)
```

These examples are verified by the plugin's [scripted test](modules/plugin/src/sbt-test/sbt-semver/basic).

## Library usage

```scala
import dev.unlu.semver.SemVer

// Parse a string (or throw via unsafe)
SemVer.of("1.2.3")
// res0: Either[String, SemVer] = Right(SemVer(1, 2, 3, None, None))
SemVer.of("not-a-version")
// res1: Either[String, SemVer] = Left("invalid SemVer string: not-a-version")
SemVer.unsafe("1.2.3")
// res2: SemVer = SemVer(1, 2, 3, None, None)

// Construct from integers
SemVer.of(1, 2, 3)
// res3: Either[String, SemVer] = Right(SemVer(1, 2, 3, None, None))
SemVer.of(1, 0, 0, preRelease = Some("rc.1"), build = Some("sha.abc"))
// res4: Either[String, SemVer] = Right(
//   SemVer(1, 0, 0, Some("rc.1"), Some("sha.abc"))
// )

// Inspect
val v = SemVer.unsafe("2.0.0-rc.1+sha.abc")
// v: SemVer = SemVer(2, 0, 0, Some("rc.1"), Some("sha.abc"))
v.major
// res5: Int = 2
v.preRelease
// res6: Option[String] = Some("rc.1")
v.render
// res7: String = "2.0.0-rc.1+sha.abc"

// Bump (pre-release and build are dropped)
val current = SemVer.unsafe("1.2.3-rc.1+sha.abc")
// current: SemVer = SemVer(1, 2, 3, Some("rc.1"), Some("sha.abc"))
current.nextMajor
// res8: SemVer = SemVer(2, 0, 0, None, None)
current.nextMinor
// res9: SemVer = SemVer(1, 3, 0, None, None)
current.nextPatch
// res10: SemVer = SemVer(1, 2, 4, None, None)

// Ordering (per spec §11; build metadata excluded from precedence)
SemVer.unsafe("1.0.0-alpha") < SemVer.unsafe("1.0.0")
// res11: Boolean = true
List(
  SemVer.unsafe("1.0.0"),
  SemVer.unsafe("1.0.0-rc.1"),
  SemVer.unsafe("1.0.0-alpha"),
  SemVer.unsafe("1.0.0-beta.2")
).sorted
// res12: List[SemVer] = List(
//   SemVer(1, 0, 0, Some("alpha"), None),
//   SemVer(1, 0, 0, Some("beta.2"), None),
//   SemVer(1, 0, 0, Some("rc.1"), None),
//   SemVer(1, 0, 0, None, None)
// )
```

All fields are validated against the [BNF grammar](https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions) at construction. For example, `1.0.0-01` is rejected because the spec forbids leading zeros in pre-release numeric identifiers. Build digits don't have that restriction, so `1.0.0+001` parses fine.

## Cats integration

`dev.unlu:semver-cats` adds `Show`, `Eq`, and `Order` instances:

```scala
import cats.Order
import cats.syntax.eq._
import cats.syntax.show._

import dev.unlu.semver.cats.instances._

SemVer.unsafe("1.2.3").show
// res13: String = "1.2.3"
SemVer.unsafe(1, 2, 3) === SemVer.unsafe(1, 2, 3)
// res14: Boolean = true
Order[SemVer].max(SemVer.unsafe("1.0.0"), SemVer.unsafe("1.0.0-rc.1"))
// res15: SemVer = SemVer(1, 0, 0, None, None)
```

## Circe integration

`dev.unlu:semver-circe` adds JSON `Encoder` and `Decoder` instances. The JSON form is the canonical string:

```scala
import io.circe.parser.decode
import io.circe.syntax._

import dev.unlu.semver.circe.instances._

SemVer.unsafe("1.2.3-rc.1").asJson
// res16: io.circe.Json = JString("1.2.3-rc.1")
decode[SemVer]("\"1.2.3-rc.1\"")
// res17: Either[io.circe.Error, SemVer] = Right(
//   SemVer(1, 2, 3, Some("rc.1"), None)
// )
decode[SemVer]("\"not-a-version\"")
// res18: Either[io.circe.Error, SemVer] = Left(
//   DecodingFailure at : invalid SemVer string: not-a-version
// )
```

## Modules

| Artifact | Purpose | Targets |
|---|---|---|
| `dev.unlu:semver-core` | `SemVer` case class + parser. Zero dependencies. | Scala 2.12, 2.13, 3.3 |
| `dev.unlu:semver-cats` | Cats typeclass instances for `SemVer`. | Scala 2.12, 2.13, 3.3 |
| `dev.unlu:semver-circe` | Circe JSON `Encoder` / `Decoder` for `SemVer`. | Scala 2.12, 2.13, 3.3 |
| `dev.unlu:sbt-semver` | AutoPlugin that re-exports `SemVer` for use in `build.sbt`. | sbt 1.x |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Apache-2.0. See [LICENSE](LICENSE).
