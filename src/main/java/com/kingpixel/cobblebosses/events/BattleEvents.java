package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingpixel.cobblebosses.util.BossTimerManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 14/02/2025 6:13
 */
public class BattleEvents {
  public static void register() {
    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, evt -> {
      ServerPlayerEntity player = null;
      for (BattleActor winnerActor : evt.getWinners()) {
        if (winnerActor instanceof PlayerBattleActor playerBattleActor) {
          player = playerBattleActor.getEntity();
        }
      }
      if (player == null) {
        if (CobbleBosses.config.isDebug()) {
          CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Player not found");
        }
        for (BattleActor winnerActor : evt.getWinners()) {
          if (winnerActor instanceof PokemonBattleActor pokemonBattleActor) {
            Pokemon pokemon = pokemonBattleActor.getPokemon().getOriginalPokemon();
            Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
            if (boss != null) {
              PokemonEntity entity = pokemonBattleActor.getEntity();
              if (entity == null) entity = pokemon.getEntity();
              if (entity != null && entity.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                boss.applyBossEffects(serverWorld, entity);
              }
            }
          }
        }
        return;
      }
      for (BattleActor loserActor : evt.getLosers()) {
        if (loserActor instanceof PokemonBattleActor pokemonBattleActor) {
          Pokemon pokemon = pokemonBattleActor.getPokemon().getOriginalPokemon();
          String id = pokemon.getPersistentData().getString(CobbleBosses.TAG_BOSS_ID);
          if (id.isEmpty()) {
            if (CobbleBosses.config.isDebug()) {
              CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Boss id not found");
            }
            continue;
          }
          Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
          if (boss == null) {
            if (CobbleBosses.config.isDebug()) {
              CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Boss not found");
            }
            continue;
          }
          boss.getRewards().giveRewards(player);
          PokemonEntity pokemonEntity = pokemonBattleActor.getEntity();
          if (pokemonEntity == null) {
            pokemonEntity = pokemon.getEntity();
          }
          if (pokemonEntity != null) {
            BossTimerManager.untrack(pokemonEntity);
            PokemonEntity finalEntity = pokemonEntity;
            if (finalEntity.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
              boss.removeBossFromTeam(serverWorld, finalEntity);
            }
            CobbleBosses.server.executeSync(() -> finalEntity.remove(Entity.RemovalReason.DISCARDED));
          }
        }
      }
    });

    CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, evt -> {
      for (BattleActor actor : evt.getBattle().getActors()) {
        if (actor instanceof PokemonBattleActor pokemonBattleActor) {
          Pokemon pokemon = pokemonBattleActor.getPokemon().getOriginalPokemon();
          Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
          if (boss != null) {
            PokemonEntity entity = pokemonBattleActor.getEntity();
            if (entity == null) entity = pokemon.getEntity();
            if (entity != null && entity.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
              boss.applyBossEffects(serverWorld, entity);
            }
          }
        }
      }
    });
  }
}
