package com.kingpixel.cobblebosses.application;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.api.model.ActiveBossView;
import com.kingpixel.cobblebosses.api.model.BossSummaryView;
import com.kingpixel.cobblebosses.api.model.PlayerBossContext;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.model.Damageable;
import com.kingpixel.cobblebosses.util.BossTimerManager;
import com.kingpixel.cobblebosses.util.BossUtil;
import com.kingpixel.cobbleutils.api.PermissionApi;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Application service providing read-only query operations and immutable viewmodels for CobbleBosses.
 */
public class BossContextService {

  private static final BossContextService INSTANCE = new BossContextService();

  public static BossContextService getInstance() {
    return INSTANCE;
  }

  /**
   * Retrieves the full boss context snapshot for a player.
   *
   * @param player the player
   * @return future with the player boss context (or null if player is null)
   */
  public CompletableFuture<PlayerBossContext> getPlayerContext(ServerPlayerEntity player) {
    if (player == null) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.supplyAsync(() -> {
      UUID playerUuid = player.getUuid();
      String playerName = player.getGameProfile() != null ? player.getGameProfile().getName() : "";
      List<ActiveBossView> ownedBosses = getActiveBossesForOwnerSync(playerUuid);
      int totalBosses = CobbleBosses.bossesConfig != null && CobbleBosses.bossesConfig.getBosses() != null
        ? CobbleBosses.bossesConfig.getBosses().size()
        : 0;
      boolean isAdmin = PermissionApi.hasPermission(player, CobbleBosses.MOD_ID + ".admin", 2);

      return new PlayerBossContext(
        playerUuid,
        playerName,
        ownedBosses,
        totalBosses,
        isAdmin
      );
    }).exceptionally(ex -> new PlayerBossContext(
      player.getUuid(),
      player.getGameProfile() != null ? player.getGameProfile().getName() : "",
      Collections.emptyList(),
      0,
      false
    ));
  }

  /**
   * Retrieves a summary view of a configured boss template by ID.
   *
   * @param bossId the boss identifier
   * @return future with the optional summary view
   */
  public CompletableFuture<Optional<BossSummaryView>> getBossSummary(String bossId) {
    if (bossId == null || bossId.isBlank()) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.bossesConfig == null) {
        return Optional.empty();
      }
      Boss boss = CobbleBosses.bossesConfig.getBoss(bossId);
      return boss != null ? Optional.of(toSummary(boss)) : Optional.empty();
    });
  }

  /**
   * Retrieves summary views of all configured boss templates.
   *
   * @return future with the list of boss summaries
   */
  public CompletableFuture<List<BossSummaryView>> getAllBossSummaries() {
    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.bossesConfig == null || CobbleBosses.bossesConfig.getBosses() == null) {
        return Collections.emptyList();
      }
      List<BossSummaryView> result = new ArrayList<>();
      for (Boss boss : CobbleBosses.bossesConfig.getBosses()) {
        if (boss != null) {
          result.add(toSummary(boss));
        }
      }
      return Collections.unmodifiableList(result);
    });
  }

  /**
   * Retrieves an active boss view by its entity UUID.
   *
   * @param entityUuid the active boss entity UUID
   * @return future with the optional active boss view
   */
  public CompletableFuture<Optional<ActiveBossView>> getActiveBoss(UUID entityUuid) {
    if (entityUuid == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.server == null) {
        return Optional.empty();
      }
      for (ServerWorld world : CobbleBosses.server.getWorlds()) {
        Entity entity = world.getEntity(entityUuid);
        if (entity instanceof PokemonEntity pokemonEntity && BossUtil.isBoss(pokemonEntity)) {
          return Optional.ofNullable(toActiveBossView(pokemonEntity, world));
        }
      }
      return Optional.empty();
    });
  }

  /**
   * Retrieves all tracked timed active bosses currently in the server.
   *
   * @return future with the list of active boss views
   */
  public CompletableFuture<List<ActiveBossView>> getAllTimedActiveBosses() {
    return CompletableFuture.supplyAsync(() -> {
      if (CobbleBosses.server == null) {
        return Collections.emptyList();
      }
      List<ActiveBossView> list = new ArrayList<>();
      Map<UUID, Long> timed = BossTimerManager.getTimedBosses();
      for (UUID entityUuid : timed.keySet()) {
        for (ServerWorld world : CobbleBosses.server.getWorlds()) {
          Entity entity = world.getEntity(entityUuid);
          if (entity instanceof PokemonEntity pokemonEntity && BossUtil.isBoss(pokemonEntity)) {
            ActiveBossView view = toActiveBossView(pokemonEntity, world);
            if (view != null) {
              list.add(view);
            }
            break;
          }
        }
      }
      return Collections.unmodifiableList(list);
    });
  }

  private List<ActiveBossView> getActiveBossesForOwnerSync(UUID ownerUuid) {
    if (ownerUuid == null || CobbleBosses.server == null) {
      return Collections.emptyList();
    }
    List<ActiveBossView> list = new ArrayList<>();
    for (UUID entityUuid : BossTimerManager.getTimedBosses().keySet()) {
      for (ServerWorld world : CobbleBosses.server.getWorlds()) {
        Entity entity = world.getEntity(entityUuid);
        if (entity instanceof PokemonEntity pokemonEntity && BossUtil.isBoss(pokemonEntity)) {
          ActiveBossView view = toActiveBossView(pokemonEntity, world);
          if (view != null && ownerUuid.equals(view.getOwnerUuid())) {
            list.add(view);
          }
          break;
        }
      }
    }
    return list;
  }

  /**
   * Transforms a {@link Boss} configuration into an immutable {@link BossSummaryView}.
   *
   * @param boss the boss model
   * @return the summary viewmodel
   */
  public BossSummaryView toSummary(Boss boss) {
    if (boss == null) return null;
    Damageable damageable = boss.getDamageable();
    boolean damageableEnabled = damageable != null && damageable.isEnabled();
    double untilLife = damageable != null && damageable.getUntilLifePercentage() != null
      ? damageable.getUntilLifePercentage()
      : 10.0D;
    boolean catchable = damageable != null && damageable.isCatchable();
    int captureLevel = damageable != null ? damageable.getLevel() : boss.getMinLevel();

    return new BossSummaryView(
      boss.getId(),
      boss.getNickName(),
      boss.isGlowing(),
      boss.getGlowingColor() != null ? boss.getGlowingColor().getName() : "",
      boss.isParticles(),
      boss.getParticleColor(),
      boss.getChance(),
      boss.getMinLevel(),
      boss.getMaxLevel(),
      boss.getMinSize(),
      boss.getMaxSize(),
      boss.getPokemons(),
      boss.getProperties(),
      damageableEnabled,
      untilLife,
      catchable,
      captureLevel
    );
  }

  /**
   * Transforms an active {@link PokemonEntity} boss into an immutable {@link ActiveBossView}.
   *
   * @param pokemonEntity the boss entity
   * @param world         the server world
   * @return the active boss viewmodel
   */
  public ActiveBossView toActiveBossView(PokemonEntity pokemonEntity, ServerWorld world) {
    if (pokemonEntity == null) return null;
    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon == null) return null;
    NbtCompound nbt = pokemon.getPersistentData();
    String bossId = nbt.getString(CobbleBosses.TAG_BOSS_ID);
    if (bossId.isEmpty()) return null;

    String species = pokemon.getSpecies() != null ? pokemon.getSpecies().getName() : "";
    int level = pokemon.getLevel();
    float currentHealth = pokemonEntity.getHealth();
    float maxHealth = pokemon.getMaxHealth();
    String worldId = world != null && world.getRegistryKey() != null
      ? world.getRegistryKey().getValue().toString()
      : "";

    UUID ownerUuid = null;
    if (nbt.contains(CobbleBosses.TAG_OWNER_UUID)) {
      try {
        ownerUuid = UUID.fromString(nbt.getString(CobbleBosses.TAG_OWNER_UUID));
      } catch (IllegalArgumentException ignored) {
        ownerUuid = null;
      }
    }
    String ownerName = nbt.contains(CobbleBosses.TAG_OWNER_NAME)
      ? nbt.getString(CobbleBosses.TAG_OWNER_NAME)
      : "";

    long despawnTime = nbt.contains(CobbleBosses.TAG_DESPAWN_TIME)
      ? nbt.getLong(CobbleBosses.TAG_DESPAWN_TIME)
      : 0L;
    long remainingSeconds = despawnTime > 0
      ? Math.max(0L, (despawnTime - System.currentTimeMillis()) / 1000L)
      : BossTimerManager.getRemainingSeconds(pokemonEntity.getUuid());

    boolean inBattle = pokemonEntity.getBattleId() != null;
    boolean catchable = !pokemon.isUncatchable();

    return new ActiveBossView(
      pokemonEntity.getUuid(),
      bossId,
      species,
      level,
      currentHealth,
      maxHealth,
      worldId,
      pokemonEntity.getX(),
      pokemonEntity.getY(),
      pokemonEntity.getZ(),
      ownerUuid,
      ownerName,
      despawnTime,
      remainingSeconds,
      inBattle,
      catchable
    );
  }
}
