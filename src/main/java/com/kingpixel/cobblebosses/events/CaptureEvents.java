package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
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
    CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.subscribe(Priority.LOWEST, evt -> {
      PokemonEntity pokemonEntity = evt.getPokemonEntity();
      Pokemon pokemon = pokemonEntity.getPokemon();
      Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
      if (boss != null && evt.getCaptureResult().isSuccessfulCapture()) {
        pokemon.setLevel(boss.getDamageable().getLevel());
        PokemonProperties.Companion.parse(boss.getProperties()).apply(pokemon);
      }
    });
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.HIGHEST, evt -> {
      ServerPlayerEntity player = evt.getPlayer();
      Pokemon pokemon = evt.getPokemon();
      Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
      if (boss != null) {
        pokemon.getPersistentData().remove(CobbleBosses.TAG_BOSS_ID);
        pokemon.setLevel(boss.getDamageable().getLevel());
        boss.getRewards().giveRewards(player.getUuid());
      }
    });
  }
}
