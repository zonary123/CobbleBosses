package com.kingpixel.cobblebosses.application;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.api.action.BossActionResult;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.util.BossTimerManager;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Application service providing headless actions for CobbleBosses spawning, reward bosses, despawning, and reloading.
 */
public class BossActionService {

  private static final BossActionService INSTANCE = new BossActionService();

  public static BossActionService getInstance() {
    return INSTANCE;
  }

  /**
   * Spawns a boss at the given player's position.
   *
   * @param player            the target player whose location is used
   * @param bossId            the boss configuration ID
   * @param pokemonProperties optional pokemon properties string (defaults to "random" if null/blank)
   * @return future with the action result
   */
  public CompletableFuture<BossActionResult> spawnBossAtPlayer(ServerPlayerEntity player,
                                                               String bossId,
                                                               String pokemonProperties) {
    if (player == null) {
      return CompletableFuture.completedFuture(BossActionResult.offline("Player is offline."));
    }
    if (bossId == null || bossId.isBlank()) {
      return CompletableFuture.completedFuture(BossActionResult.bossNotFound("Boss ID is invalid.", ""));
    }
    if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) {
      return CompletableFuture.completedFuture(BossActionResult.worldNotFound("Player world is unavailable."));
    }
    return spawnBossAtLocation(serverWorld, player.getPos(), bossId, pokemonProperties);
  }

  /**
   * Spawns a boss at the specified world and coordinates.
   *
   * @param world             the server world
   * @param pos               the spawn coordinates
   * @param bossId            the boss configuration ID
   * @param pokemonProperties optional pokemon properties string (defaults to "random" if null/blank)
   * @return future with the action result
   */
  public CompletableFuture<BossActionResult> spawnBossAtLocation(ServerWorld world,
                                                                 Vec3d pos,
                                                                 String bossId,
                                                                 String pokemonProperties) {
    if (world == null || pos == null) {
      return CompletableFuture.completedFuture(BossActionResult.worldNotFound("World or position is null."));
    }
    if (bossId == null || bossId.isBlank()) {
      return CompletableFuture.completedFuture(BossActionResult.bossNotFound("Boss ID is invalid.", ""));
    }

    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.bossesConfig == null) {
        return BossActionResult.failure(BossActionResult.Status.ERROR, "BossesConfig is not initialized.");
      }
      Boss boss = CobbleBosses.bossesConfig.getBoss(bossId);
      if (boss == null) {
        return BossActionResult.bossNotFound("Boss not found: " + bossId, bossId);
      }

      String props = (pokemonProperties == null || pokemonProperties.isBlank()) ? "random" : pokemonProperties;
      Pokemon pokemon = PokemonProperties.Companion.parse(props).create();
      boss.spawn(world, pos, pokemon);
      return BossActionResult.success("Boss spawned successfully.", boss.getId());
    }).exceptionally(ex -> BossActionResult.failure(BossActionResult.Status.ERROR, ex.getMessage(), bossId));
  }

  /**
   * Spawns a player-locked reward boss with an automatic despawn timer.
   *
   * @param player            the target owner player
   * @param bossId            the boss configuration ID
   * @param durationSeconds   the duration in seconds before timeout (uses default if <= 0)
   * @param pokemonProperties optional pokemon properties string (defaults to "random" if null/blank)
   * @return future with the action result
   */
  public CompletableFuture<BossActionResult> spawnRewardBoss(ServerPlayerEntity player,
                                                             String bossId,
                                                             int durationSeconds,
                                                             String pokemonProperties) {
    if (player == null) {
      return CompletableFuture.completedFuture(BossActionResult.offline("Player is offline."));
    }
    if (bossId == null || bossId.isBlank()) {
      return CompletableFuture.completedFuture(BossActionResult.bossNotFound("Boss ID is invalid.", ""));
    }

    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.bossesConfig == null) {
        return BossActionResult.failure(BossActionResult.Status.ERROR, "BossesConfig is not initialized.");
      }
      Boss boss = CobbleBosses.bossesConfig.getBoss(bossId);
      if (boss == null) {
        return BossActionResult.bossNotFound("Boss not found: " + bossId, bossId);
      }
      if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) {
        return BossActionResult.worldNotFound("Player world is unavailable.");
      }

      int resolvedDuration = durationSeconds > 0
        ? durationSeconds
        : (CobbleBosses.config != null ? CobbleBosses.config.getDefaultRewardDuration() : 300);

      String props = (pokemonProperties == null || pokemonProperties.isBlank()) ? "random" : pokemonProperties;
      Pokemon pokemon = PokemonProperties.Companion.parse(props).create();
      boss.spawn(serverWorld, player.getPos(), pokemon, player, resolvedDuration);

      if (CobbleBosses.language != null && CobbleBosses.config != null) {
        String formattedTime = BossTimerManager.formatTime(resolvedDuration);
        PlayerUtils.sendMessage(
          player,
          CobbleBosses.language.getBossSpawnedReward()
            .replace("%boss%", boss.getId())
            .replace("%time%", formattedTime),
          CobbleBosses.config.getPrefix(),
          TypeMessage.CHAT
        );
      }

      return BossActionResult.success("Reward boss spawned successfully.", boss.getId());
    }).exceptionally(ex -> BossActionResult.failure(BossActionResult.Status.ERROR, ex.getMessage(), bossId));
  }

  /**
   * Grants a configured boss's rewards directly to a player headlessly.
   *
   * @param player the target player
   * @param bossId the boss configuration ID
   * @return future with the action result
   */
  public CompletableFuture<BossActionResult> grantBossRewards(ServerPlayerEntity player, String bossId) {
    if (player == null) {
      return CompletableFuture.completedFuture(BossActionResult.offline("Player is offline."));
    }
    if (bossId == null || bossId.isBlank()) {
      return CompletableFuture.completedFuture(BossActionResult.bossNotFound("Boss ID is invalid.", ""));
    }

    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.bossesConfig == null) {
        return BossActionResult.failure(BossActionResult.Status.ERROR, "BossesConfig is not initialized.");
      }
      Boss boss = CobbleBosses.bossesConfig.getBoss(bossId);
      if (boss == null) {
        return BossActionResult.bossNotFound("Boss not found: " + bossId, bossId);
      }
      if (boss.getRewards() != null) {
        boss.getRewards().giveRewards(player);
      }
      return BossActionResult.success("Boss rewards granted successfully.", boss.getId());
    }).exceptionally(ex -> BossActionResult.failure(BossActionResult.Status.ERROR, ex.getMessage(), bossId));
  }

  /**
   * Despawns an active boss entity by its entity UUID.
   *
   * @param entityUuid the UUID of the active boss entity
   * @return future with the action result
   */
  public CompletableFuture<BossActionResult> despawnBoss(UUID entityUuid) {
    if (entityUuid == null) {
      return CompletableFuture.completedFuture(BossActionResult.entityNotFound("Entity UUID is null."));
    }

    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.server == null) {
        return BossActionResult.failure(BossActionResult.Status.ERROR, "Minecraft server is not running.");
      }
      boolean despawned = BossTimerManager.despawnActiveBoss(CobbleBosses.server, entityUuid);
      if (!despawned) {
        return BossActionResult.entityNotFound("Active boss entity not found: " + entityUuid);
      }
      return BossActionResult.success("Active boss despawned successfully.", "", entityUuid);
    }).exceptionally(ex -> BossActionResult.failure(BossActionResult.Status.ERROR, ex.getMessage()));
  }

  /**
   * Reloads CobbleBosses configurations, languages, and boss templates headlessly.
   *
   * @return future with the action result
   */
  public CompletableFuture<BossActionResult> reloadConfigs() {
    return CompletableFuture.supplyAsync(() -> {
      CobbleBosses.load();
      return BossActionResult.success("CobbleBosses configurations reloaded.", "");
    }).exceptionally(ex -> BossActionResult.failure(BossActionResult.Status.ERROR, ex.getMessage()));
  }
}
