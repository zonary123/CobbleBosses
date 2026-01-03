package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 *
 * @author Carlos Varas Alonso - 03/01/2026 5:05
 */
public class CaptureEvents {
  public static void register() {
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, evt -> {
      ServerPlayerEntity player = evt.getPlayer();
      Pokemon pokemon = evt.getPokemon();
      Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
      if (boss != null) {
        pokemon.getPersistentData().remove(CobbleBosses.TAG_BOSS_ID);
        boss.getRewards().giveRewards(player.getUuid());
      }
    });
  }
}
