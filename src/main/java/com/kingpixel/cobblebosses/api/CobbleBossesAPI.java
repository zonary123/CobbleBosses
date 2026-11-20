package com.kingpixel.cobblebosses.api;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.api.action.BossActionResult;
import com.kingpixel.cobblebosses.api.model.ActiveBossView;
import com.kingpixel.cobblebosses.api.model.BossSummaryView;
import com.kingpixel.cobblebosses.api.model.PlayerBossContext;
import com.kingpixel.cobblebosses.api.ui.BossMenuProvider;
import com.kingpixel.cobblebosses.api.ui.BossMenuRegistry;
import com.kingpixel.cobblebosses.application.BossActionService;
import com.kingpixel.cobblebosses.application.BossContextService;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.util.BossUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Unified public API facade for CobbleBosses.
 */
public final class CobbleBossesAPI {

  private CobbleBossesAPI() {
  }

  // --- UI Menu Provider & Registry API ---

  public static void openMainMenu(ServerPlayerEntity player) {
    BossMenuRegistry.openMainMenu(player);
  }

  public static void openBossRewardMenu(ServerPlayerEntity player, Boss boss) {
    BossMenuRegistry.openBossRewardMenu(player, boss);
  }

  public static void openBossRewardMenu(ServerPlayerEntity player, String bossId) {
    BossMenuRegistry.openBossRewardMenu(player, bossId);
  }

  public static void registerMenuProvider(BossMenuProvider provider) {
    BossMenuRegistry.register(provider);
  }

  public static void unregisterMenuProvider() {
    BossMenuRegistry.unregister();
  }

  public static boolean hasCustomMenuProvider() {
    return BossMenuRegistry.hasCustomProvider();
  }

  public static BossMenuProvider getMenuProvider() {
    return BossMenuRegistry.getProvider();
  }

  // --- Headless Action API ---

  public static CompletableFuture<BossActionResult> spawnBossAtPlayer(ServerPlayerEntity player,
                                                                      String bossId,
                                                                      String pokemonProperties) {
    return BossActionService.getInstance().spawnBossAtPlayer(player, bossId, pokemonProperties);
  }

  public static CompletableFuture<BossActionResult> spawnBossAtLocation(ServerWorld world,
                                                                        Vec3d pos,
                                                                        String bossId,
                                                                        String pokemonProperties) {
    return BossActionService.getInstance().spawnBossAtLocation(world, pos, bossId, pokemonProperties);
  }

  public static CompletableFuture<BossActionResult> spawnRewardBoss(ServerPlayerEntity player,
                                                                    String bossId,
                                                                    int durationSeconds,
                                                                    String pokemonProperties) {
    return BossActionService.getInstance().spawnRewardBoss(player, bossId, durationSeconds, pokemonProperties);
  }

  public static CompletableFuture<BossActionResult> grantBossRewards(ServerPlayerEntity player, String bossId) {
    return BossActionService.getInstance().grantBossRewards(player, bossId);
  }

  public static CompletableFuture<BossActionResult> despawnBoss(UUID entityUuid) {
    return BossActionService.getInstance().despawnBoss(entityUuid);
  }

  public static CompletableFuture<BossActionResult> reloadConfigs() {
    return BossActionService.getInstance().reloadConfigs();
  }

  // --- Context & Query API ---

  public static CompletableFuture<PlayerBossContext> getPlayerContext(ServerPlayerEntity player) {
    return BossContextService.getInstance().getPlayerContext(player);
  }

  public static CompletableFuture<Optional<BossSummaryView>> getBossSummary(String bossId) {
    return BossContextService.getInstance().getBossSummary(bossId);
  }

  public static CompletableFuture<List<BossSummaryView>> getAllBossSummaries() {
    return BossContextService.getInstance().getAllBossSummaries();
  }

  public static CompletableFuture<Optional<ActiveBossView>> getActiveBoss(UUID entityUuid) {
    return BossContextService.getInstance().getActiveBoss(entityUuid);
  }

  public static CompletableFuture<List<ActiveBossView>> getAllTimedActiveBosses() {
    return BossContextService.getInstance().getAllTimedActiveBosses();
  }

  // --- Direct Model & Utility Queries ---

  public static Boss getBossById(String bossId) {
    if (bossId == null || CobbleBosses.bossesConfig == null) return null;
    return CobbleBosses.bossesConfig.getBoss(bossId);
  }

  public static List<Boss> getConfiguredBosses() {
    if (CobbleBosses.bossesConfig == null || CobbleBosses.bossesConfig.getBosses() == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(CobbleBosses.bossesConfig.getBosses());
  }

  public static boolean isBoss(Entity entity) {
    return BossUtil.isBoss(entity);
  }

  public static boolean isBoss(PokemonEntity pokemonEntity) {
    return BossUtil.isBoss(pokemonEntity);
  }
}
