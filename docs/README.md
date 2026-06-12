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

```scala mdoc
import dev.unlu.semver.SemVer

// Parse a string (or throw via unsafe)
SemVer.of("1.2.3")
SemVer.of("not-a-version")
SemVer.unsafe("1.2.3")

// Construct from integers
SemVer.of(1, 2, 3)
SemVer.of(1, 0, 0, preRelease = Some("rc.1"), build = Some("sha.abc"))

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
List(
  SemVer.unsafe("1.0.0"),
  SemVer.unsafe("1.0.0-rc.1"),
  SemVer.unsafe("1.0.0-alpha"),
  SemVer.unsafe("1.0.0-beta.2")
).sorted
```

All fields are validated against the [BNF grammar](https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions) at construction. For example, `1.0.0-01` is rejected because the spec forbids leading zeros in pre-release numeric identifiers. Build digits don't have that restriction, so `1.0.0+001` parses fine.

## Cats integration

`dev.unlu:semver-cats` adds `Show`, `Eq`, and `Order` instances:

```scala mdoc
import cats.Order
import cats.syntax.eq._
import cats.syntax.show._

import dev.unlu.semver.cats.instances._

SemVer.unsafe("1.2.3").show
SemVer.unsafe(1, 2, 3) === SemVer.unsafe(1, 2, 3)
Order[SemVer].max(SemVer.unsafe("1.0.0"), SemVer.unsafe("1.0.0-rc.1"))
```

## Circe integration

`dev.unlu:semver-circe` adds JSON `Encoder` and `Decoder` instances. The JSON form is the canonical string:

```scala mdoc
import io.circe.parser.decode
import io.circe.syntax._

import dev.unlu.semver.circe.instances._

SemVer.unsafe("1.2.3-rc.1").asJson
decode[SemVer]("\"1.2.3-rc.1\"")
decode[SemVer]("\"not-a-version\"")
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
