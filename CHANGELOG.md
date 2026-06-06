# CHANGELOG

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-06-06

Initial release.

### Added

- `semver-core`: typed `SemVer` with BNF-validated parser, `Ordering[SemVer]` per spec §11, and `nextMajor`/`nextMinor`/`nextPatch` helpers.
- `semver-cats`: `Show`, `Eq`, and `Order` typeclass instances.
- `sbt-semver`: AutoPlugin re-exporting `SemVer` for use in `build.sbt`.

Cross-built for Scala 2.12, 2.13, and 3.3. Targets JDK 8 bytecode. Apache-2.0.

[0.1.0]: https://github.com/oguzhanunlu/sbt-semver/releases/tag/0.1.0
