# Contributing

## Setup (per clone)

```bash
git config core.hooksPath .githooks
```

This enables the pre-commit hook, which runs `sbt fmt` before each commit so formatting issues never reach CI.

## Common commands

- `sbt +test` runs tests across Scala 2.12 / 2.13 / 3.
- `sbt fmt` formats Scala and `.sbt` files.
- `sbt fmtCheck` verifies formatting (what CI runs).
- `sbt docs/mdoc` regenerates `README.md` from `docs/README.md`.

## Editing docs

The root `README.md` is generated from `docs/README.md`. Edit the template; run `sbt docs/mdoc` to regenerate. The CI lint job fails if the two are out of sync.
