# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.0] - Unreleased

First playable version.

### Added
- Portable Motors in three tiers (copper, brass, diamond): battery-powered Create generators with selectable RPM that
  keep their charge and speed when picked up with a wrench.
- Motor energy drain that grows with the selected RPM, plus a cost for the connected load.
- Solar Panels in three tiers, generating FE from sunlight, reduced at night and in rain.
- Energy Cable with automatic connections and local energy levelling.
- Charge bar and tooltip on the motor item; maximum output tooltip on the solar panel item.
- Ponder scenes for the motor, battery charging, and solar panels with cables.
- JEI and EMI description pages for every item.
- Start-up sound and sparks when an empty motor regains energy; a subtle glint on producing solar panels.
- Per-tier balance in `serverconfig/create_pocketdynamo-server.toml`.
- English and Spanish (`es_es`) translations, including Ponder scenes and viewer pages.

### Tests
- GameTests for the motor battery (charging, draining, restarting, cost by RPM), keeping charge and RPM when picked
  up with a wrench, how the Energy Cable shares energy, and solar output by day, at night and in the rain. Run them
  with `./gradlew runGameTestServer`; CI runs them on every pull request.

### Known limitations
- Textures, models and the logo are placeholders.
- Cables only carry energy that producers push into them.
- Not covered by the GameTests, and not yet tested by hand either:
  - EMI at runtime (it needs a client; the GameTests run on a headless server).
  - Several players at once (for example two people wrenching or charging the same motor).
  - The motor on moving contraptions (a motor picked up by a Create contraption, or carried on one).
  - Rendering, sounds and particles, which are client-side.
  - Chunk unloading and reloading of a charged motor or a cable line.
