# 📖 CobbleBosses Complete Wiki & Configuration Guide

Welcome to the **CobbleBosses** documentation! CobbleBosses is an advanced wild Pokémon boss encounter and world challenge engine designed for Minecraft Cobblemon servers. It transforms ordinary wild Pokémon into towering, high-level world bosses equipped with glowing auras, custom particle trails, levels exceeding standard level caps (Level 100+), rich defeat loot tables, and a balanced weakening-and-capture system.

---

## 📌 Table of Contents
1. [Overview & Core Architecture](#-overview--core-architecture)
2. [Commands & Permissions](#-commands--permissions)
3. [Boss Mechanics & Lifecycle](#-boss-mechanics--lifecycle)
   - [Natural Spawning Conversion](#natural-spawning-conversion)
   - [Overleveling Engine (Level 100+ Bypass)](#overleveling-engine-level-100-bypass)
   - [Visual Scale, Glowing & Particle Auras](#visual-scale-glowing--particle-auras)
   - [Weakening & Capture Mechanics (Damageable)](#weakening--capture-mechanics-damageable)
4. [Directory & File Organization](#-directory--file-organization)
5. [Complete JSON Configuration Schema](#-complete-json-configuration-schema)
   - [Global Settings (`config.json`)](#global-settings-configjson)
   - [Boss Definition Schema (`bosses/<id>.json`)](#boss-definition-schema-bossesidjson)
   - [Damageable Sub-Schema](#damageable-sub-schema)
   - [AdvancedItemChance Reward Schema](#advanceditemchance-reward-schema)
6. [Production JSON Configuration Examples](#-production-json-configuration-examples)
   - [Example 1: Wild Giant Raichu Boss (Weakening & Catchable)](#example-1-wild-giant-raichu-boss-weakening--catchable)
   - [Example 2: Mythical World Raid Boss (Uncatchable, Level 150)](#example-2-mythical-world-raid-boss-uncatchable-level-150)
7. [Troubleshooting & FAQ](#-troubleshooting--faq)

---

## 🌟 Overview & Core Architecture

- **Natural Wild Spawning Hook**: Integrates directly with Cobblemon's spawning pipeline (`SpawningEvents`). Any wild Pokémon spawn can roll a configurable chance to transform into an intimidating boss encounter.
- **Level Cap Transgression**: Standard Cobblemon restricts Pokémon levels to 100. CobbleBosses dynamically bypasses the global cap for active boss entities (supporting Level 120, 150, or higher) and restores the level seamlessly across server restarts and chunk unloads using NBT serialization (`boss_real_level`).
- **Dynamic Scoreboard Team Auras**: Boss entities are automatically registered into vanilla Scoreboard Teams with custom glow colors (`glowingColor`), casting glowing outlines through blocks.
- **Weakening & Fair Capture (Damageable)**: Bosses are uncatchable by default. However, enabling the `damageable` module allows trainers to battle and weaken the boss below a specific HP threshold (e.g., 10% health) to unlock PokéBall capture. Once caught, the Pokémon can automatically re-scale to a balanced trainer level (e.g. Level 10 or 25) to preserve server balance.
- **Custom Defeat Payouts**: Awards items, currency, commands, and broadcast announcements asynchronously via CobbleUtils' `AdvancedItemChance` loot engine.

---

## 💻 Commands & Permissions

Base command aliases (configurable in `config.json`): `/cobblebosses`, `/boss`

### Staff & Admin Commands
*(Requires permission `cobblebosses.admin` or Minecraft Level 2 / OP)*

| Command | Arguments | Description |
| :--- | :--- | :--- |
| `/boss reload` | *None* | Hot-reloads all configurations, language strings, and boss files in `config/cobblebosses/bosses/`. |
| `/boss migrate` | *None* | Converts legacy single-file boss configurations into modular individual boss files. |
| `/boss spawn <boss_id> <player> [properties]` | `<boss> <player> [props]` | Spawns a specific boss at the target player's exact location with optional Cobblemon properties (e.g. `shiny=true`). |
| `/boss spawn <boss_id> <pos> <world> [properties]` | `<boss> <x y z> <world> [props]` | Spawns a specific boss at designated world coordinates. |
| `/boss reward <player> <boss_id> [duration] [properties]` | `<player> <boss> [duration] [props]` | Spawns a private reward boss locked exclusively to the target player with an auto-despawn timer (in seconds). |
| `/boss spawnreward <boss_id> <player> [duration] [properties]` | `<boss> <player> [duration] [props]` | Alias for `/boss reward` using boss-first argument order. |

---

## ⚡ Boss Mechanics & Lifecycle

### Player-Locked Reward Bosses & Auto-Despawn Timer
Designed specifically for server rewards, voting systems, daily prizes, and quest completions:
- **Anti-Theft Exclusive Lock**: Only the recipient player can initiate battle or deal direct damage to the boss. Any other player attempting to battle or hit the entity is immediately blocked and notified.
- **Defeat Countdown Timer**: Bosses spawned via `/boss reward` or `/boss spawnreward` have a configurable countdown timer (specified per command or falling back to `defaultRewardDuration` in `config.json`).
- **60-Second Warning & Auto-Despawn**: Sends an alert to the owner when 60 seconds remain. If not defeated within the time limit, any active battle is safely terminated and the entity despawns cleanly from the world.

### Natural Spawning Conversion
When wild Pokémon spawn around players, CobbleBosses evaluates matching boss configurations:
1. Checks if the spawning species is in the boss's `pokemons` list (or `["*"]` for any).
2. Rolls against `chance` (e.g., `0.05` = 5% chance).
3. If successful, despawns the regular wild Pokémon and replaces it with the upgraded Boss entity.

### Overleveling Engine (Level 100+ Bypass)
If a boss specifies `"minLevel": 110` and `"maxLevel": 130`:
- CobbleBosses temporarily raises Cobblemon's internal max level cap.
- Sets the wild Pokémon's level within that range.
- Writes the real level into `boss_real_level` inside the entity's persistent NBT.
- Restores the global level cap so regular wild Pokémon and player parties remain capped at 100.
- When chunks reload, the `ENTITY_LOAD` listener reads `boss_real_level` and re-applies the boss attributes.

### Visual Scale, Glowing & Particle Auras
- **Size Scaling**: Configurable via `minSize` and `maxSize` (e.g. `2.5f` makes the boss 2.5x larger than normal).
- **Glowing Aura**: Uses vanilla Minecraft glowing effects with configurable colors (`LIGHT_PURPLE`, `RED`, `GOLD`, `AQUA`).
- **Particles**: Emits colored particle clouds around the boss entity in the world using hex colors (e.g. `"#FF0000"`).

### Weakening & Capture Mechanics (Damageable)
Prevent players from instantly throwing a Master Ball at a level 150 boss on turn 1:
- `catchable: false`: PokéBalls bounce off with a custom warning message.
- `damageable.enabled: true`: Once trainers reduce the boss's HP below `untilLifePercentage` (e.g. `10.0`), the boss enters a weakened state.
- In the weakened state, `catchable` unlocks!
- Upon capture, the Pokémon is transformed according to `damageable.level` (e.g., resets to Level 15) and `damageable.properties` so players don't retain an overpowered raid boss.

---

## 📁 Directory & File Organization

All configurations and boss definitions reside inside `config/cobblebosses/`:

```text
config/cobblebosses/
├── config.json          # Main command aliases, prefix, and debug mode
├── WIKI.md              # Auto-extracted in-game reference guide
├── lang/
│   └── en.json          # Translatable messages, reload feedback, and catch notices
└── bosses/              # Modular boss definition files
    ├── giant_raichu.json
    └── legendary_boss.json
```

---

## 📝 Complete JSON Configuration Schema

### Global Settings (`config/cobblebosses/config.json`)

```json
{
  "prefix": "&8[&6CobbleBosses&8] ",
  "commands": [
    "cobblebosses",
    "boss"
  ],
  "debug": false
}
```

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `prefix` | `String` | `"&8[&6CobbleBosses&8] "` | Chat prefix displayed before mod notifications. Supports color codes. |
| `commands` | `List<String>` | `["cobblebosses", "boss"]` | Command aliases registered in Minecraft to access admin commands. |
| `debug` | `Boolean` | `false` | Enables verbose console logging for spawn checks and battle hooks. |

---

### Boss Definition Schema (`config/cobblebosses/bosses/<id>.json`)

```json
{
  "id": "giant_raichu",
  "nickName": "&e&l%pokemon% &6&lBoss",
  "glowing": true,
  "glowingColor": "GOLD",
  "particles": true,
  "particleColor": "#FFD700",
  "chance": 0.05,
  "minLevel": 110,
  "maxLevel": 125,
  "minSize": 2.2,
  "maxSize": 2.8,
  "pokemons": [
    "raichu",
    "pikachu"
  ],
  "properties": "shiny=true",
  "damageable": { ... },
  "rewards": { ... }
}
```

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `id` | `String` | `"default"` | Unique identifier for this boss setup. |
| `nickName` | `String` | `"%pokemon% Boss"` | Custom overhead nametag. `%pokemon%` is replaced with the species name. |
| `glowing` | `Boolean` | `true` | Enables glowing team aura around the boss entity. |
| `glowingColor` | `String` | `"LIGHT_PURPLE"` | Minecraft formatting color name (`GOLD`, `RED`, `AQUA`, `LIGHT_PURPLE`, etc.). |
| `particles` | `Boolean` | `true` | Emits particle clouds around the boss. |
| `particleColor` | `String` | `"#CBC3E3"` | Hex color string for particle effects. |
| `chance` | `Float` | `0.1` | Probability weight for natural wild spawn conversion (e.g. `0.05` = 5%). |
| `minLevel` | `Integer` | `100` | Minimum level rolled for this boss (supports 100+). |
| `maxLevel` | `Integer` | `120` | Maximum level rolled for this boss. |
| `minSize` | `Float` | `1.5` | Minimum model scale factor. |
| `maxSize` | `Float` | `2.0` | Maximum model scale factor. |
| `pokemons` | `List<String>` | `["pikachu"]` | List of Pokémon species eligible to convert into this boss (or `["*"]` for any). |
| `properties` | `String` | `"shiny=true"` | Cobblemon properties injected onto the spawned boss (aspects, IVs, shiny). |
| `damageable` | `Object` | *See below* | Weakening and capture settings. |
| `rewards` | `Object` | *See below* | Defeat reward drops and console commands. |

---

### Damageable Sub-Schema

```json
{
  "enabled": true,
  "untilLifePercentage": 15.0,
  "catchable": true,
  "level": 25,
  "properties": "shiny=false"
}
```

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enabled` | `Boolean` | `false` | Enables the weakening mechanic. If `false`, the boss cannot be captured. |
| `untilLifePercentage` | `Double` | `10.0` | Health percentage threshold. The boss only becomes catchable when current HP is below this value. |
| `catchable` | `Boolean` | `true` | Whether PokéBalls are allowed once the boss is weakened. |
| `level` | `Integer` | `10` | The level the Pokémon is scaled down to upon being successfully captured. |
| `properties` | `String` | `"level=10"` | Additional Cobblemon properties applied to the caught Pokémon upon capture. |

---

## 💡 Production JSON Configuration Examples

### Example 1: Wild Giant Raichu Boss (Weakening & Catchable)
A natural encounter that spawns at Level 115-125 with gold glowing particles, rewarding high-tier items on defeat, or catchable if weakened below 15% HP:

```json
{
  "id": "giant_raichu",
  "nickName": "&e&lGiant Raichu &6&l[Boss]",
  "glowing": true,
  "glowingColor": "GOLD",
  "particles": true,
  "particleColor": "#FFD700",
  "chance": 0.05,
  "minLevel": 115,
  "maxLevel": 125,
  "minSize": 2.2,
  "maxSize": 2.5,
  "pokemons": [
    "raichu",
    "alolan_raichu"
  ],
  "properties": "shiny=true",
  "damageable": {
    "enabled": true,
    "untilLifePercentage": 15.0,
    "catchable": true,
    "level": 30,
    "properties": "shiny=true"
  },
  "rewards": {
    "id": "raichu_boss_loot",
    "showMenu": false,
    "giveAll": true,
    "cumulativeLootTable": false,
    "amountRewardsPermission": { "": 1 },
    "animation": "NONE",
    "newSound": {
      "sound": "minecraft:entity.player.levelup",
      "volume": 1.0,
      "pitch": 1.0
    },
    "lootTable": {
      "": [
        {
          "chance": 100.0,
          "item": "cobblemon:thunder_stone",
          "minAmount": 2,
          "maxAmount": 4,
          "commands": [
            "eco give %player% 2500",
            "broadcast &e%player% &7has defeated the &6Giant Raichu Boss&7!"
          ]
        },
        {
          "chance": 40.0,
          "item": "cobblemon:ability_capsule",
          "minAmount": 1,
          "maxAmount": 1,
          "commands": []
        }
      ]
    }
  }
}
```

---

### Example 2: Mythical World Raid Boss (Uncatchable, Level 150)
A server raid boss with massive scale (3.0x), red glowing aura, completely uncatchable, with grand broadcast rewards:

```json
{
  "id": "rayquaza_world_boss",
  "nickName": "&c&lAncient Rayquaza &4&l[WORLD BOSS]",
  "glowing": true,
  "glowingColor": "RED",
  "particles": true,
  "particleColor": "#FF0000",
  "chance": 0.0,
  "minLevel": 150,
  "maxLevel": 150,
  "minSize": 3.0,
  "maxSize": 3.0,
  "pokemons": [
    "rayquaza"
  ],
  "properties": "",
  "damageable": {
    "enabled": false,
    "untilLifePercentage": 0.0,
    "catchable": false,
    "level": 70,
    "properties": ""
  },
  "rewards": {
    "id": "rayquaza_raid_loot",
    "showMenu": true,
    "title": "&c&lWorld Boss Defeated!",
    "giveAll": true,
    "cumulativeLootTable": false,
    "amountRewardsPermission": { "": 1 },
    "animation": "CSGO",
    "newSound": {
      "sound": "minecraft:ui.toast.challenge_complete",
      "volume": 1.0,
      "pitch": 0.8
    },
    "lootTable": {
      "": [
        {
          "chance": 100.0,
          "item": "cobblemon:master_ball",
          "minAmount": 3,
          "maxAmount": 5,
          "displayname": "&5&lMaster Ball Pack",
          "commands": [
            "broadcast &4&lWORLD BOSS: &cAncient Rayquaza &7has been slain by &6%player%&7!",
            "eco give %player% 50000"
          ]
        }
      ]
    }
  }
}
```

---

## ❓ Troubleshooting & FAQ

### Can I manually spawn a boss for an event?
Yes! Use the admin spawn command:
```bash
/boss spawn giant_raichu Steve
```
Or spawn at exact coordinates:
```bash
/boss spawn rayquaza_world_boss 100 64 200 minecraft:overworld
```

### Why did the boss level reset to 100 after a server restart?
Ensure you are using the latest version of CobbleBosses. The mod automatically stores and restores the level via `boss_real_level` NBT tags during the `ENTITY_LOAD` event.

### How do I prevent players from capturing a boss with a Master Ball?
Set `"damageable": { "enabled": false, "catchable": false }`. When `catchable` is false, CobbleBosses intercepts PokéBall throws during battle and prevents the capture.
