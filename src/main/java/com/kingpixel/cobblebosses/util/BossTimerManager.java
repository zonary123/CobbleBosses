package com.kingpixel.cobblebosses.util;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active timers and automatic despawning for reward bosses.
 *
 * @author Carlos Varas Alonso
 */
public class BossTimerManager {
  private static final Map<UUID, Long> timedBosses = new ConcurrentHashMap<>();
  private static final Set<UUID> warned60s = ConcurrentHashMap.newKeySet();
  private static boolean initialized = false;
  private static int tickCounter = 0;

  private BossTimerManager() {
    // Utility class
  }

  public static synchronized void init() {
    if (initialized) return;
    initialized = true;
    TickEvent.SERVER_POST.register(BossTimerManager::tick);
  }

  public static void track(PokemonEntity entity, int durationSeconds) {
    if (entity == null || durationSeconds <= 0) return;
    long despawnTime = System.currentTimeMillis() + (durationSeconds * 1000L);
    timedBosses.put(entity.getUuid(), despawnTime);
  }

  public static void track(PokemonEntity entity) {
    if (entity == null) return;
    Pokemon pokemon = entity.getPokemon();
    if (pokemon == null) return;
    NbtCompound nbt = pokemon.getPersistentData();
    if (!nbt.contains(CobbleBosses.TAG_DESPAWN_TIME)) return;
    long despawnTime = nbt.getLong(CobbleBosses.TAG_DESPAWN_TIME);
    timedBosses.put(entity.getUuid(), despawnTime);
  }

  public static void untrack(PokemonEntity entity) {
    if (entity != null) {
      untrack(entity.getUuid());
    }
  }

  public static void untrack(UUID entityUuid) {
    if (entityUuid != null) {
      timedBosses.remove(entityUuid);
      warned60s.remove(entityUuid);
    }
  }

  private static void tick(MinecraftServer server) {
    if (server == null) return;
    tickCounter++;
    if (tickCounter % 20 != 0) return;

    if (timedBosses.isEmpty()) return;

    long now = System.currentTimeMillis();

    for (Map.Entry<UUID, Long> entry : timedBosses.entrySet()) {
      UUID entityUuid = entry.getKey();
      long despawnTime = entry.getValue();
      long remainingMillis = despawnTime - now;

      if (remainingMillis <= 0) {
        untrack(entityUuid);
        despawnBoss(server, entityUuid);
      } else if (remainingMillis <= 60000 && !warned60s.contains(entityUuid)) {
        warned60s.add(entityUuid);
        notifyWarning(server, entityUuid, (int) (remainingMillis / 1000));
      }
    }
  }

  private static void notifyWarning(MinecraftServer server, UUID entityUuid, int remainingSeconds) {
    PokemonEntity pokemonEntity = findPokemonEntity(server, entityUuid);
    if (pokemonEntity == null) return;

    ServerPlayerEntity owner = resolveOwner(server, pokemonEntity);
    if (owner != null) {
      PlayerUtils.sendMessage(
        owner,
        CobbleBosses.language.getBossTimeoutWarning()
          .replace("%time%", formatTime(remainingSeconds)),
        CobbleBosses.config.getPrefix(),
        TypeMessage.CHAT
      );
    }
  }

  private static void despawnBoss(MinecraftServer server, UUID entityUuid) {
    PokemonEntity pokemonEntity = findPokemonEntity(server, entityUuid);
    if (pokemonEntity == null) return;

    ServerPlayerEntity owner = resolveOwner(server, pokemonEntity);
    if (owner != null) {
      PlayerUtils.sendMessage(
        owner,
        CobbleBosses.language.getBossTimeout(),
        CobbleBosses.config.getPrefix(),
        TypeMessage.CHAT
      );
    }

    if (pokemonEntity.getBattleId() != null) {
      PokemonBattle battle = BattleRegistry.getBattle(pokemonEntity.getBattleId());
      if (battle != null) {
        battle.stop();
      }
    }

    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon != null && pokemonEntity.getEntityWorld() instanceof ServerWorld serverWorld) {
      Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
      if (boss != null) {
        boss.removeBossFromTeam(serverWorld, pokemonEntity);
      }
    }

    server.executeSync(() -> pokemonEntity.remove(Entity.RemovalReason.DISCARDED));
  }

  private static PokemonEntity findPokemonEntity(MinecraftServer server, UUID entityUuid) {
    for (ServerWorld world : server.getWorlds()) {
      Entity entity = world.getEntity(entityUuid);
      if (entity instanceof PokemonEntity pokemonEntity) {
        return pokemonEntity;
      }
    }
    return null;
  }

  private static ServerPlayerEntity resolveOwner(MinecraftServer server, PokemonEntity pokemonEntity) {
    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon == null) return null;

    NbtCompound nbt = pokemon.getPersistentData();
    String ownerUuidStr = nbt.getString(CobbleBosses.TAG_OWNER_UUID);
    if (ownerUuidStr.isEmpty()) return null;

    try {
      UUID ownerUuid = UUID.fromString(ownerUuidStr);
      return server.getPlayerManager().getPlayer(ownerUuid);
    } catch (IllegalArgumentException ignored) {
      // Invalid UUID string in NBT
      return null;
    }
  }

  public static String formatTime(long seconds) {
    if (seconds >= 3600) {
      long hours = seconds / 3600;
      long minutes = (seconds % 3600) / 60;
      long secs = seconds % 60;
      return String.format("%02d:%02d:%02d", hours, minutes, secs);
    } else {
      long minutes = seconds / 60;
      long secs = seconds % 60;
      return String.format("%02d:%02d", minutes, secs);
    }
  }
}
