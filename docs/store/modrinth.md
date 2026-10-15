# Modrinth page for Create: Pocket Dynamo

Text and settings ready to paste into <https://modrinth.com/project/create-pocket-dynamo/settings>. Everything below the
"Description" heading goes into the description box. Keep this file in sync with the README when the mod changes.

## Settings

| Field | Value |
|---|---|
| Name | Create: Pocket Dynamo |
| URL (slug) | `create-pocket-dynamo` |
| Summary | Battery-powered portable motors for Create. Charge them with solar panels and energy cables, wrench them up with their charge and speed intact, and carry rotational force anywhere. |
| Categories | Technology |
| Client side | Required |
| Server side | Required |
| License | MIT |
| Source code | https://github.com/ShadowPJ/create_pocketdynamo |
| Issue tracker | https://github.com/ShadowPJ/create_pocketdynamo/issues |
| Icon | `docs/store/icon-512.png` (512x512, the mod logo scaled without blur) |

## Version to upload

| Field | Value |
|---|---|
| Version number | `0.1.0-beta.1` |
| Version title | `0.1.0-beta.1` |
| Release channel | **Beta** |
| Loaders | NeoForge |
| Game versions | 1.21.1 |
| File | `create_pocketdynamo-0.1.0-beta.1.jar` from the GitHub Release |
| Dependencies | **Create** (required), **JEI** (optional), **EMI** (optional) |
| Changelog | the `0.1.0-beta.1` entry of `CHANGELOG.md` |

If a dependency is not found by its slug (`create`, `jei`, `emi`), search for it by name in the dependency picker.

## Gallery

Upload in this order. The first one is the image shown in listings, so mark it as featured.

| Image | Title | Description |
|---|---|---|
| Wide shot with the solar field, the cable, the motor and the machines | Solar power, anywhere | Solar panels feed a Portable Motor through Energy Cables, and the motor drives Create machines. |
| The three tiers side by side | Three tiers | Copper, brass and diamond motors and panels, with the battery shown on each motor. |
| The diamond motor next to a panel | Charged by sunlight | A Diamond Portable Motor charged by a solar panel. |
| The seven recipe images from `docs/images/` | Recipes | One image per recipe, titled with the item name. |

## Description

**Rotational Force you can put in your pocket.**

[Create](https://modrinth.com/mod/create) normally ties its power to a spot: a water wheel needs a river, a steam
engine needs a boiler. Pocket Dynamo adds a **battery-powered motor you carry with you**. Charge it, set the speed, plug
it into whatever you are building, and when you are done, wrench it back into your inventory. It remembers its charge and
its speed.

Three tiers of motors, three tiers of solar panels to keep them fed, and an energy cable to bring the power from a field
of panels to wherever you work.

> **Beta.** Everything works and is covered by automated tests, but the art is a first draft and block ids may still
> change before 1.0. Feedback is very welcome.

## How it plays

1. **Charge it.** The motor has an internal battery that fills with Forge Energy (FE) from any block placed next to it.
   A solar panel right beside the motor is enough.
2. **Put it to work.** Place the motor, open its value panel and choose the speed and direction, exactly like Create's
   Creative Motor, up to the limit of its tier.
3. **Take it with you.** Sneak and use a Create Wrench on it. The motor goes to your inventory **with its charge and its
   selected speed**, and the item shows a charge bar. Place it somewhere else and it carries on.

When the battery runs dry the motor stops. It starts again by itself, with a small spark and a start-up sound, as soon as
it gets energy.

## Portable Motors

| Tier | Max speed | Stress capacity | Battery | Charges at up to | Speed cost |
|---|---|---|---|---|---|
| Copper | 32 RPM | 16,384 SU | 200,000 FE | 1,000 FE/t | 0.30 FE/t per RPM |
| Brass | 64 RPM | 49,152 SU | 400,000 FE | 2,000 FE/t | 0.25 FE/t per RPM |
| Diamond | 256 RPM | 262,144 SU | 2,000,000 FE | 8,000 FE/t | 0.20 FE/t per RPM |

While it spins, a motor pays a cost for every RPM it is set to, **even with nothing connected**, plus a cost proportional
to the stress it carries. Faster costs more, so set it only as fast as you need. Create's Engineer's Goggles show the
battery and the drain. With nothing attached, a full battery lasts roughly 17 minutes (copper at 32 RPM), 21 minutes
(brass at 64 RPM) or 33 minutes (diamond at 256 RPM).

## Solar Panels

| Tier | Output at full sun | Buffer |
|---|---|---|
| Copper | 20 FE/t | 2,000 FE |
| Brass | 60 FE/t | 6,000 FE |
| Diamond | 180 FE/t | 18,000 FE |

They need open sky above them and a dimension that has skylight, such as the Overworld. Output follows the daylight (full
at noon, roughly a quarter at midnight), and rain cuts it to a quarter or less.

## Energy Cable

A plain FE cable that connects by itself to other cables and to any block that holds energy, including blocks from other
mods. Energy spreads by local levelling, up to 2,000 FE per tick per segment. Cables never pull energy out of other
blocks: producers push into them.

## Recipes

Every recipe uses only vanilla and Create items. The recipe images are in the gallery, and JEI or EMI show them in game.

## Compatibility

- Minecraft 1.21.1, NeoForge 21.1.219 or newer, Create 6.0.11 or newer in the 6.0 line, Java 21.
- The motor battery accepts FE from other mods, and the panels feed any block that accepts FE.
- JEI and EMI are optional and add a description page for each item. Install one of them, not both.
- Ponder scenes explain the motor, the battery and the panels: hold <kbd>W</kbd> over an item.
- English and Spanish translations.

## Configuration

All the numbers above are server settings, one file per world: `serverconfig/create_pocketdynamo-server.toml`. You can
change the speed, stress, battery and energy costs of each motor tier, the output and buffer of each panel tier, the rain
multiplier and the cable throughput.

## Links

[Source code](https://github.com/ShadowPJ/create_pocketdynamo) ·
[Report a problem](https://github.com/ShadowPJ/create_pocketdynamo/issues) ·
[Changelog](https://github.com/ShadowPJ/create_pocketdynamo/blob/main/CHANGELOG.md)
