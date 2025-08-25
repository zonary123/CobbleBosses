package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.config.BossesConfig;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import kotlin.Unit;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 14/02/2025 4:18
 */
public class SpawningEvents {

  public static void register() {
    CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGHEST, evt -> {
      CompletableFuture.runAsync(() -> {
          var pokemonEntity = evt.getEntity();
          var pokemon = pokemonEntity.getPokemon();
          if (isSpecial(pokemon)) return;
          ServerWorld world = (ServerWorld) pokemonEntity.getEntityWorld();
          String s = world.getRegistryKey().getValue().toString();
          if (CobbleBosses.config.isDebug()) {
            CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "World: " + s);
          }
          if (CobbleBosses.config.getBlackListWorlds().contains(s)) return;
          int random = Utils.RANDOM.nextInt(CobbleBosses.config.getRateSpawn());
          if (random == 0) {
            var boss = BossesConfig.getRandomBoss();
            if (boss == null) return;
            boss.convert(pokemonEntity);
            CobbleBosses.server.executeSync(evt::cancel);
          }
        })
        .exceptionally(e -> {
          e.printStackTrace();
          return null;
        });
      return Unit.INSTANCE;
    });
  }

  private static boolean isSpecial(Pokemon p) {
    NbtCompound nbt = p.getPersistentData();
    return PokemonUtils.getIvsAverage(p.getIvs()) >= 31 || p.isLegendary() || p.getShiny() || nbt.contains(CobbleBosses.TAG_BOSS_ID);
  }
}
