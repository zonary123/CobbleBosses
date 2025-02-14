package com.kingpixel.cobblebosses.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.config.BossesConfig;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import kotlin.Unit;
import net.minecraft.nbt.NbtCompound;

/**
 * @author Carlos Varas Alonso - 14/02/2025 4:18
 */
public class SpawningEvents {

  public static void register() {
    CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGHEST, evt -> {
      var pokemonEntity = evt.getEntity();
      var pokemon = pokemonEntity.getPokemon();
      if (isSpecial(pokemon)) return Unit.INSTANCE;
      int random = Utils.RANDOM.nextInt(CobbleBosses.config.getRateSpawn());
      if (random == 0) {
        var boss = BossesConfig.getRandomBoss();
        if (boss == null) return Unit.INSTANCE;
        boss.convert(pokemonEntity);
        evt.cancel();
      }
      return Unit.INSTANCE;
    });
  }

  private static boolean isSpecial(Pokemon p) {
    NbtCompound nbt = p.getPersistentData();
    return PokemonUtils.getIvsAverage(p.getIvs()) >= 31 || p.isLegendary() || p.getShiny() || nbt.contains(CobbleBosses.TAG_BOSS_ID);
  }
}
