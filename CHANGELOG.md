# Changelog

## [1.1.3] - 2026-10-18

### Added

- **Player-Locked Reward Bosses & Auto-Despawn Timer**:
  - Added `/boss reward <player> <boss> [duration] [pokemon]` and `/boss spawnreward <boss> <player> [duration] [pokemon]`.
  - Anti-theft protection: bosses spawned via this command are exclusively bound to the target player. Other players are prevented from battling or dealing direct damage to the boss.
  - Defeat Countdown Timer: reward bosses feature an auto-despawn timer (default 300s, configurable in `config.json`). Sends a warning when 60s remain, and automatically despawns the boss (terminating any active battle safely) if not defeated in time.
- **Automatic In-Mod Wiki**:
  - The mod now automatically generates a comprehensive `WIKI.md` guide inside the `config/cobblebosses/` folder on server startup. Server owners can view all boss settings, commands, permissions, and scaling configuration examples locally without searching for external documentation.

### Bug Fixes

- Fixed an issue where boss titles or tags (such as «Épico») would repeat endlessly every time the boss reloaded or finished a battle.
- Fixed boss glowing outlines not appearing properly or disappearing after battles and healing.
- Fixed bosses disappearing and naturally despawning when players moved away.
- Fixed an issue where damageable bosses without catch enabled became immortal at 1 HP instead of being defeated and dropping loot.
- Fixed players not being notified when a boss's health is high enough to block battles or low enough to start fighting.
- Fixed potential errors and crashes when checking player teams, banned moves, or loading boss configuration files.
- Fixed scoreboard buildup caused by defeated or captured bosses remaining in color teams.

## [1.1.2] - 2026-09-06

### Changed

- **Cobblemon 1.8.0 Compatibility**: Updated compatibility and dependencies to support Cobblemon 1.8.0 (Minecraft 1.21.1).

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
