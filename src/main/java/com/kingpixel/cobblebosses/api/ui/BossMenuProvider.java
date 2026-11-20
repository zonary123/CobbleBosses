package com.kingpixel.cobblebosses.api.ui;

import com.kingpixel.cobblebosses.model.Boss;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Service Provider Interface (SPI) for CobbleBosses user interfaces.
 * Allows companion mods (e.g. Rotom Phone, custom graphical screens, web panels)
 * to intercept and replace default chest GUI menus with custom screens.
 */
public interface BossMenuProvider {

  /**
   * Opens the main boss overview / directory menu for a player.
   *
   * @param player the player
   */
  void openMainMenu(ServerPlayerEntity player);

  /**
   * Opens the reward preview menu for a specific Boss instance.
   *
   * @param player the player
   * @param boss   the boss template
   */
  void openBossRewardMenu(ServerPlayerEntity player, Boss boss);

  /**
   * Opens the reward preview menu for a specific Boss ID.
   *
   * @param player the player
   * @param bossId the boss identifier
   */
  void openBossRewardMenu(ServerPlayerEntity player, String bossId);
}
