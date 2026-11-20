package com.kingpixel.cobblebosses.api.ui;

import com.kingpixel.cobblebosses.model.Boss;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe registry for CobbleBosses UI menu providers.
 * Allows companion mods (e.g. Rotom Phone, custom GUIs) to register custom menu providers.
 * If no custom provider is registered, it automatically falls back to {@link DefaultGooeyBossMenuProvider}.
 */
public final class BossMenuRegistry {

  private static final BossMenuProvider DEFAULT_PROVIDER = new DefaultGooeyBossMenuProvider();
  private static final AtomicReference<BossMenuProvider> CUSTOM_PROVIDER = new AtomicReference<>(null);

  private BossMenuRegistry() {
  }

  /**
   * Registers a custom menu provider. Passing null unregisters any custom provider.
   *
   * @param provider the custom menu provider, or null to revert to default
   */
  public static void register(BossMenuProvider provider) {
    CUSTOM_PROVIDER.set(provider);
  }

  /**
   * Unregisters the custom menu provider, reverting back to the default GooeyLibs implementation.
   */
  public static void unregister() {
    CUSTOM_PROVIDER.set(null);
  }

  /**
   * Checks whether a custom menu provider is currently registered.
   *
   * @return true if a custom provider is active
   */
  public static boolean hasCustomProvider() {
    return CUSTOM_PROVIDER.get() != null;
  }

  /**
   * Returns the active menu provider (custom if present, otherwise default).
   *
   * @return the active menu provider
   */
  public static BossMenuProvider getProvider() {
    BossMenuProvider custom = CUSTOM_PROVIDER.get();
    return custom != null ? custom : DEFAULT_PROVIDER;
  }

  /**
   * Opens the main boss menu using the active provider.
   *
   * @param player the player
   */
  public static void openMainMenu(ServerPlayerEntity player) {
    if (player == null) return;
    getProvider().openMainMenu(player);
  }

  /**
   * Opens the reward preview menu for a specific Boss using the active provider.
   *
   * @param player the player
   * @param boss   the boss template
   */
  public static void openBossRewardMenu(ServerPlayerEntity player, Boss boss) {
    if (player == null || boss == null) return;
    getProvider().openBossRewardMenu(player, boss);
  }

  /**
   * Opens the reward preview menu for a specific Boss ID using the active provider.
   *
   * @param player the player
   * @param bossId the boss identifier
   */
  public static void openBossRewardMenu(ServerPlayerEntity player, String bossId) {
    if (player == null || bossId == null || bossId.isBlank()) return;
    getProvider().openBossRewardMenu(player, bossId);
  }
}
