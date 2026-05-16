package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;

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
        var player = battle.getPlayers().getFirst();
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon1 : party) {
          for (Move move : pokemon1.getMoveSet()) {
            if (CobbleBosses.config.getBanMoves().contains(move.getName())) {
              PlayerUtils.sendMessage(
                player,
                PokemonUtils.replace(CobbleBosses.language.getMoveBanned()
                    .replace("%move%", move.getName()),
                  pokemon1),
                CobbleBosses.config.getPrefix(),
                TypeMessage.CHAT
              );
              evt.cancel();
              return;
            }
          }
        }
        var damageable = boss.getDamageable();
        if (damageable.isEnabled() && damageable.isDownLife(pokemonEntity)) {
          evt.cancel();
          return;
        }
      }
    });
  }
}
