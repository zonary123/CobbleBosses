# Changelog

## [1.1.1] - 2026-08-02

### Bug Fixes

- Fixed an issue where boss entities were not removed from the world after being defeated in Cobblemon battles.
- Fixed premature loop exit during battle victory event handling when multiple non-boss actors were in the battle's loser list.
- Fixed overworld damageable bosses (with `catchable = false`) remaining alive at 1 HP instead of being despawned when health reached 0.
- Fixed non-damageable battle bosses opening reward menus when attacked in the overworld.

## [1.1.0] - 16-05-2026

## Features

- Now you can ban moves to prevent players from using them in boss battles. This is optional.

## [1.0.8] - 30-03-2026

## Features

- Now you can ban moves to prevent players from using them in boss battles. This is optional.

## Bug Fixes

- Fixed a crash with ledger.

## [1.0.7] - 08-02-2026

### Features

- No new features added.

## Bug Fixes

- Now the bosses don’t drop the loot table as if they were normal wild Pokémon
- Fixed crash when the capturable bosses option was enabled and the boss had a level above Cobblemon's maximum level.
- Added properties option to the damageable option for captures.

## [1.0.6] - 2025-12-03

### Features

- Added by `eupedroosouza`. Bosses can now be configured so you can fight them with swords, and once they reach low
  health, they can be set as capturable or flightless. This is optional.

### Bug Fixes

- Fixed an issue where files in folders were being written to bosses instead of their corresponding folder.
- Crashed when you enter a battle from a fishing encounter.

### Optimizations

- N\A
