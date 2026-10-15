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

### Known limitations
- Textures, models and the logo are placeholders.
- Cables only carry energy that producers push into them.
- Not yet tested with EMI at runtime, with multiple players, or with the motor on moving contraptions.
