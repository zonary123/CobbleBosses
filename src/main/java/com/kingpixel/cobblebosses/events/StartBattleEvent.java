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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

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
        if (battle.getPlayers().isEmpty()) continue;
        var player = battle.getPlayers().getFirst();
        if (player == null) continue;

        NbtCompound nbt = pokemon.getPersistentData();
        if (nbt.contains(CobbleBosses.TAG_OWNER_UUID)) {
          String ownerUuidStr = nbt.getString(CobbleBosses.TAG_OWNER_UUID);
          for (ServerPlayerEntity p : battle.getPlayers()) {
            if (!p.getUuid().toString().equalsIgnoreCase(ownerUuidStr)) {
              String ownerName = nbt.getString(CobbleBosses.TAG_OWNER_NAME);
              PlayerUtils.sendMessage(
                p,
                CobbleBosses.language.getNotYourBoss()
                  .replace("%owner%", ownerName)
                  .replace("%player%", p.getGameProfile().getName()),
                CobbleBosses.config.getPrefix(),
                TypeMessage.CHAT
              );
              evt.cancel();
              return;
            }
          }
        }
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        int slot = 0;
        for (Pokemon pokemon1 : party) {
          slot++;
          if (pokemon1 == null) continue;
          for (Move move : pokemon1.getMoveSet()) {
            if (move != null && CobbleBosses.config.getBanMoves().contains(move.getName())) {
              PlayerUtils.sendMessage(
                player,
                PokemonUtils.replace(CobbleBosses.language.getMoveBanned()
                    .replace("%move%", move.getName())
                    .replace("%slot%", String.valueOf(slot)),
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
          PlayerUtils.sendMessage(
            player,
            CobbleBosses.language.getCannotFightYet(),
            CobbleBosses.config.getPrefix(),
            TypeMessage.CHAT
          );
          evt.cancel();
          return;
        }
      }
    });
  }
}
