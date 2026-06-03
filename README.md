# sbt-semver

SemVer for Scala. BNF-modeled, zero deps, sbt plugin included.

```scala
import dev.unlu.semver.SemVer

// From a string
SemVer.of("1.2.3")               // Right(SemVer(1.2.3))
SemVer.of("not-a-version")       // Left("invalid SemVer string: not-a-version")
SemVer.unsafe("1.2.3")           // SemVer or throws on invalid

// From integers
SemVer.of(1, 2, 3)               // Right(SemVer(1.2.3))
SemVer.of(-1, 0, 0)              // Left("major must be >= 0, got -1")
SemVer.unsafe(1, 2, 3)           // SemVer or throws on invalid

// Inspect
val v = SemVer.unsafe("2.0.0-rc.1+sha.abc")
v.major                          // 2
v.preRelease                     // Some("rc.1")
v.render                         // "2.0.0-rc.1+sha.abc"
```

All fields are validated against the [BNF grammar](https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions) at construction. For example, `1.0.0-01` is rejected because the spec forbids leading zeros in pre-release numeric identifiers. Build digits don't have that restriction, so `1.0.0+001` parses fine.

## Modules

| Artifact | Purpose | Targets |
|---|---|---|
| `dev.unlu:semver-core` | `SemVer` case class + parser. Zero dependencies. | Scala 2.12, 2.13, 3.3 |
| `dev.unlu:sbt-semver` | AutoPlugin that re-exports `SemVer` for use in `build.sbt`. | sbt 1.x |

## License

Apache-2.0. See [LICENSE](LICENSE).
