package com.kingpixel.cobblebosses.api.ui;

import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Default GooeyLibs-based implementation of {@link BossMenuProvider}.
 */
public class DefaultGooeyBossMenuProvider implements BossMenuProvider {

  @Override
  public void openMainMenu(ServerPlayerEntity player) {
    if (player == null) return;
    if (CobbleBosses.bossesConfig == null) return;
    List<Boss> bosses = CobbleBosses.bossesConfig.getBosses();
    if (bosses == null || bosses.isEmpty()) return;
    openBossRewardMenu(player, bosses.getFirst());
  }

  @Override
  public void openBossRewardMenu(ServerPlayerEntity player, Boss boss) {
    if (player == null || boss == null) return;
    AdvancedItemChance rewards = boss.getRewards();
    if (rewards != null) {
      rewards.openMenu(player, template -> {
      }, close -> {
      });
    }
  }

  @Override
  public void openBossRewardMenu(ServerPlayerEntity player, String bossId) {
    if (player == null || bossId == null || bossId.isBlank()) return;
    if (CobbleBosses.bossesConfig == null) return;
    Boss boss = CobbleBosses.bossesConfig.getBoss(bossId);
    if (boss != null) {
      openBossRewardMenu(player, boss);
    }
  }
}
