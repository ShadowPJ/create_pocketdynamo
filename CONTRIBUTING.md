# Contributing

Thanks for helping with Create: Pocket Dynamo. This page covers what you need to build the addon and what a pull
request should contain.

## Setup

You need a JDK 21 and nothing else; Gradle comes with the wrapper.

```
./gradlew build      # jar in build/libs
./gradlew runClient  # dev client with Create (and JEI by default)
./gradlew runData    # regenerates src/generated/resources
./gradlew runGameTestServer  # runs the GameTests on a headless server
```

## Before you open a pull request

1. **Build:** `./gradlew build` passes. If you changed how a motor, solar panel or cable behaves, also run
   `./gradlew runGameTestServer`; the tests live in `src/gametest` and are not packed into the jar.
2. **Generated files:** if you touched blocks, items, recipes, loot or English text, run `./gradlew runData` and commit
   the changes under `src/generated/resources`. They are committed on purpose.
3. **Translations:** after changing any text, run `java tools/LangCheck.java`. It fails when `es_es.json` is missing a
   key or has mismatched placeholders. `es_es.json` is written by hand.
4. **Ponder scenes:** the `.nbt` structures are generated. Edit `tools/PonderStructureGen.java` and re-run it as shown
   in the README, rather than editing the files.
5. **Changelog:** add a line to the entry of the version in progress in `CHANGELOG.md` for anything a player would
   notice.

The CI workflow runs the build, the GameTests, the translation check, the Gradle wrapper validation and a secret scan
on every pull request. It has to pass before merging.

## Dependencies

Dependency checksums are verified through `gradle/verification-metadata.xml`. When you add or update a dependency
(including Create, NeoForge or JEI), regenerate it and commit the result:

```
./gradlew --write-verification-metadata sha256 build
```

Check the diff: only the artifacts you expect should appear.

## Commits and pull requests

- Use [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `docs:`, `build:`, `ci:`,
  `chore:`. Keep the subject short and in the imperative.
- One topic per pull request. Link the issue it solves with `Closes #123`.
- Pull requests are squash-merged, so the pull request title becomes the commit message.

## Secrets

Never commit tokens, keys, passwords or `.env` files. `.gitignore` covers the usual names, but it is not a safety net.
If you committed one by mistake, tell the maintainer right away: the secret has to be revoked, not just deleted.

## Reporting problems

Use the issue templates for bugs and ideas. For security problems follow [SECURITY.md](SECURITY.md) instead.

By contributing you agree that your work is released under the [MIT license](LICENSE).
