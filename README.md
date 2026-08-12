# Civilization AI

> "The villagers are no longer NPCs. They become a living civilization."

A Minecraft Forge 1.20.1 mod that turns villages into autonomous,
emergent civilizations: villagers take on dynamic jobs based on real
needs, build without pre-scripted blueprints, pave roads where they
actually walk, and grow an economy, technology tree, and defense
response — all driven by simulation state rather than scripts.

This repository was built in seven sequential phases (see
`docs/ROADMAP.md`) and **all seven are now complete.** The mod is
playable end-to-end: any real Villager entity that spawns, breeds, or
loads into the world is automatically claimed by a nearby civilization or
founds a new one, and every system this mod implements (jobs, resources,
economy, buildings, roads, population, relationships, reputation,
incidents, technology, defense) runs on it from there — inspectable via
`/civilization` commands and extendable by other mods via a small public
API. See `docs/ARCHITECTURE.md` for what's genuinely implemented per
system versus documented as a deliberate scope cut — every simplification
is called out explicitly rather than left silent.

## Requirements

- Java Development Kit 17
- Minecraft Forge 1.20.1 (MDK, `forge_version` pinned in `gradle.properties`)
- Gradle 8.x (via the wrapper — see note below)

## One-time setup: the Gradle wrapper jar

For reproducibility this generator does not ship a binary
`gradle/wrapper/gradle-wrapper.jar` (binary artifacts aren't produced by
the tool that generated this scaffold). Before your first build, do
**one** of the following:

1. If you have Gradle 8.x installed locally, run:
   ```
   gradle wrapper --gradle-version 8.1.1
   ```
   from the project root — this generates the missing jar in place.
2. Or download it directly from the official Gradle repository and place
   it at `gradle/wrapper/gradle-wrapper.jar`:
   ```
   https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
   ```
3. Or open the project directly in IntelliJ IDEA with the Minecraft
   Development plugin, which will fetch Gradle for you without needing
   the wrapper jar at all.

## Building

```
./gradlew build
```

The compiled mod jar is written to `build/libs/`.

## Running in a dev environment

```
./gradlew runClient   # launches a client with the mod loaded
./gradlew runServer   # launches a dedicated server with the mod loaded
```

## Commands

| Command | Access | Does |
|---|---|---|
| `/civilization info` | everyone | Summary of the nearest civilization |
| `/civilization list` | everyone | Every civilization in your dimension |
| `/civilization stats` | everyone | Server-wide totals |
| `/civilization reputation <player>` | everyone | A player's reputation with the nearest civilization |
| `/civilization debug` | op (permission 2) | Needs, jobs, incidents, economy, and performance stats for the nearest civilization |
| `/civilization create <name>` | op | Found a civilization at your position |
| `/civilization reset` | op | Remove the nearest civilization |
| `/civilization export` | op | Write the nearest civilization's state to a file |
| `/civilization import <file>` | op | Restore a civilization's economic/technological state from a file |

## Project layout

```
src/main/java/MineGamer/civilizationai/    see docs/DEVELOPER_GUIDE.md for the full package map
src/test/java/MineGamer/civilizationai/    real JUnit 5 tests against the domain layer
src/main/resources/
  META-INF/mods.toml         Mod metadata
  pack.mcmeta                Resource/data pack version info
  assets/civilizationai/lang lang files
docs/
  ARCHITECTURE.md            System design, per-phase design record
  ROADMAP.md                 Phase-by-phase implementation plan
  DEVELOPER_GUIDE.md          Package map, "where does new code go," testing
  API.md                      Public API for other mods
  CONFIGURATION.md            Full config reference
  PERFORMANCE.md              How the mod stays fast, and how to check
  EXPANSION_GUIDE.md          Ideas for extending this mod's own source
```

## Configuration

A single COMMON config file is generated on first launch at
`config/civilizationai-common.toml` — see `docs/CONFIGURATION.md` for the
full reference, or every value's own in-file comment.

## API for other mods

See `docs/API.md`.

## License

See `LICENSE`.
