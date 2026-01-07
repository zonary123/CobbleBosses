package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;

/**
 *
 * @author Carlos Varas Alonso - 03/01/2026 5:19
 */
public class StartBattleEvent {
  public static void register() {
    CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGHEST, evt -> {
      PokemonBattle battle = evt.getBattle();
      if (!battle.isPvW()) return;
      var actors = battle.getActors();
      for (BattleActor actor : actors) {
        if (!(actor instanceof PokemonBattleActor pokemonBattleActor)) continue;
        PokemonEntity pokemonEntity = pokemonBattleActor.getEntity();
        if (pokemonEntity == null) continue;
        Pokemon pokemon = pokemonEntity.getPokemon();
        var boss = CobbleBosses.bossesConfig.getBoss(pokemon);
        if (boss == null) continue;
        var damageable = boss.getDamageable();
        if (damageable.isEnabled() && damageable.isDownLife(pokemonEntity)) {
          evt.cancel();
          return;
        }
      }
    });
  }
}
