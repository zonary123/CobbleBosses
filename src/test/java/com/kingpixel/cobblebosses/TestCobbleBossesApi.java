package com.kingpixel.cobblebosses;

import com.kingpixel.cobblebosses.api.CobbleBossesAPI;
import com.kingpixel.cobblebosses.api.action.BossActionResult;
import com.kingpixel.cobblebosses.api.model.ActiveBossView;
import com.kingpixel.cobblebosses.api.model.BossSummaryView;
import com.kingpixel.cobblebosses.api.model.PlayerBossContext;
import com.kingpixel.cobblebosses.api.ui.BossMenuProvider;
import com.kingpixel.cobblebosses.api.ui.BossMenuRegistry;
import com.kingpixel.cobblebosses.api.ui.DefaultGooeyBossMenuProvider;
import com.kingpixel.cobblebosses.application.BossActionService;
import com.kingpixel.cobblebosses.application.BossContextService;
import com.kingpixel.cobblebosses.model.Boss;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCobbleBossesApi {

  @BeforeEach
  @AfterEach
  void resetRegistry() {
    BossMenuRegistry.unregister();
  }

  @Test
  void testDefaultMenuProvider() {
    assertFalse(BossMenuRegistry.hasCustomProvider());
    assertNotNull(BossMenuRegistry.getProvider());
    assertTrue(BossMenuRegistry.getProvider() instanceof DefaultGooeyBossMenuProvider);
  }

  @Test
  void testCustomMenuProviderRegistration() {
    AtomicBoolean mainOpened = new AtomicBoolean(false);
    AtomicBoolean rewardOpened = new AtomicBoolean(false);

    BossMenuProvider custom = new BossMenuProvider() {
      @Override
      public void openMainMenu(ServerPlayerEntity player) {
        mainOpened.set(true);
      }

      @Override
      public void openBossRewardMenu(ServerPlayerEntity player, Boss boss) {
        rewardOpened.set(true);
      }

      @Override
      public void openBossRewardMenu(ServerPlayerEntity player, String bossId) {
        rewardOpened.set(true);
      }
    };

    CobbleBossesAPI.registerMenuProvider(custom);
    assertTrue(CobbleBossesAPI.hasCustomMenuProvider());
    assertSame(custom, CobbleBossesAPI.getMenuProvider());

    // Null player guard should prevent invocation on null player
    CobbleBossesAPI.openMainMenu(null);
    assertFalse(mainOpened.get());
    CobbleBossesAPI.openBossRewardMenu(null, "default");
    assertFalse(rewardOpened.get());

    CobbleBossesAPI.unregisterMenuProvider();
    assertFalse(CobbleBossesAPI.hasCustomMenuProvider());
    assertTrue(CobbleBossesAPI.getMenuProvider() instanceof DefaultGooeyBossMenuProvider);
  }

  @Test
  void testBossActionResultStatuses() {
    UUID entityId = UUID.randomUUID();
    BossActionResult success = BossActionResult.success("Spawned!", "epic_boss", entityId);
    assertTrue(success.isSuccess());
    assertEquals(BossActionResult.Status.SUCCESS, success.getStatus());
    assertEquals("Spawned!", success.getMessage());
    assertEquals("epic_boss", success.getBossId());
    assertEquals(entityId, success.getEntityUuid());

    BossActionResult offline = BossActionResult.offline("Player is offline");
    assertFalse(offline.isSuccess());
    assertEquals(BossActionResult.Status.PLAYER_OFFLINE, offline.getStatus());

    BossActionResult notFound = BossActionResult.bossNotFound("Boss not found", "missing_boss");
    assertFalse(notFound.isSuccess());
    assertEquals(BossActionResult.Status.BOSS_NOT_FOUND, notFound.getStatus());
    assertEquals("missing_boss", notFound.getBossId());

    BossActionResult entityNotFound = BossActionResult.entityNotFound("No entity");
    assertFalse(entityNotFound.isSuccess());
    assertEquals(BossActionResult.Status.ENTITY_NOT_FOUND, entityNotFound.getStatus());

    BossActionResult worldNotFound = BossActionResult.worldNotFound("No world");
    assertFalse(worldNotFound.isSuccess());
    assertEquals(BossActionResult.Status.WORLD_NOT_FOUND, worldNotFound.getStatus());

    BossActionResult noPerm = BossActionResult.noPermission("No permission");
    assertFalse(noPerm.isSuccess());
    assertEquals(BossActionResult.Status.NO_PERMISSION, noPerm.getStatus());
  }

  @Test
  void testBossSummaryViewImmutability() {
    BossSummaryView view = new BossSummaryView(
      "legendary",
      "&6%pokemon% &cBoss",
      true,
      "GOLD",
      true,
      "#FFD700",
      0.25f,
      100,
      150,
      1.5f,
      2.5f,
      List.of("rayquaza", "mewtwo"),
      "shiny=true",
      true,
      15.0,
      true,
      70
    );

    assertEquals("legendary", view.getId());
    assertEquals("&6%pokemon% &cBoss", view.getNickName());
    assertTrue(view.isGlowing());
    assertEquals("GOLD", view.getGlowingColor());
    assertTrue(view.isParticles());
    assertEquals("#FFD700", view.getParticleColor());
    assertEquals(0.25f, view.getChance());
    assertEquals(100, view.getMinLevel());
    assertEquals(150, view.getMaxLevel());
    assertEquals(1.5f, view.getMinSize());
    assertEquals(2.5f, view.getMaxSize());
    assertEquals(2, view.getPokemons().size());
    assertEquals("shiny=true", view.getProperties());
    assertTrue(view.isDamageableEnabled());
    assertEquals(15.0, view.getUntilLifePercentage());
    assertTrue(view.isCatchable());
    assertEquals(70, view.getCaptureLevel());

    assertThrows(UnsupportedOperationException.class, () -> view.getPokemons().add("pikachu"));
  }

  @Test
  void testActiveBossView() {
    UUID entityUuid = UUID.randomUUID();
    UUID ownerUuid = UUID.randomUUID();
    ActiveBossView view = new ActiveBossView(
      entityUuid,
      "default",
      "Pikachu",
      120,
      250.0f,
      500.0f,
      "minecraft:overworld",
      100.5,
      64.0,
      -200.25,
      ownerUuid,
      "TrainerAsh",
      1700000300000L,
      180L,
      false,
      true
    );

    assertEquals(entityUuid, view.getEntityUuid());
    assertEquals("default", view.getBossId());
    assertEquals("Pikachu", view.getSpecies());
    assertEquals(120, view.getLevel());
    assertEquals(250.0f, view.getCurrentHealth());
    assertEquals(500.0f, view.getMaxHealth());
    assertEquals("minecraft:overworld", view.getWorldId());
    assertTrue(view.hasOwner());
    assertEquals(ownerUuid, view.getOwnerUuid());
    assertTrue(view.getOwnerUuidOptional().isPresent());
    assertEquals("TrainerAsh", view.getOwnerName());
    assertEquals(180L, view.getRemainingSeconds());
    assertFalse(view.isInBattle());
    assertTrue(view.isCatchable());
  }

  @Test
  void testPlayerBossContext() {
    UUID playerUuid = UUID.randomUUID();
    PlayerBossContext ctx = new PlayerBossContext(
      playerUuid,
      "TrainerRed",
      List.of(),
      4,
      true
    );

    assertEquals(playerUuid, ctx.getPlayerUuid());
    assertEquals("TrainerRed", ctx.getPlayerName());
    assertFalse(ctx.hasActiveOwnedBosses());
    assertEquals(0, ctx.getActiveOwnedBossCount());
    assertEquals(4, ctx.getTotalConfiguredBosses());
    assertTrue(ctx.isHasAdminPermission());
    assertThrows(UnsupportedOperationException.class, () -> ctx.getOwnedActiveBosses().add(null));
  }

  @Test
  void testNullAndOfflineGuardsInActionService() {
    BossActionService service = BossActionService.getInstance();

    BossActionResult r1 = service.spawnBossAtPlayer(null, "default", "random").join();
    assertFalse(r1.isSuccess());
    assertEquals(BossActionResult.Status.PLAYER_OFFLINE, r1.getStatus());

    BossActionResult r2 = service.spawnBossAtLocation(null, null, "default", "random").join();
    assertFalse(r2.isSuccess());
    assertEquals(BossActionResult.Status.WORLD_NOT_FOUND, r2.getStatus());

    BossActionResult r3 = service.spawnRewardBoss(null, "default", 300, "random").join();
    assertFalse(r3.isSuccess());
    assertEquals(BossActionResult.Status.PLAYER_OFFLINE, r3.getStatus());

    BossActionResult r4 = service.grantBossRewards(null, "default").join();
    assertFalse(r4.isSuccess());
    assertEquals(BossActionResult.Status.PLAYER_OFFLINE, r4.getStatus());

    BossActionResult r5 = service.despawnBoss(null).join();
    assertFalse(r5.isSuccess());
    assertEquals(BossActionResult.Status.ENTITY_NOT_FOUND, r5.getStatus());
  }

  @Test
  void testNullAndOfflineGuardsInContextService() {
    BossContextService service = BossContextService.getInstance();

    assertNull(service.getPlayerContext(null).join());
    assertFalse(service.getBossSummary(null).join().isPresent());
    assertFalse(service.getBossSummary("").join().isPresent());
    assertFalse(service.getActiveBoss(null).join().isPresent());
  }
}
