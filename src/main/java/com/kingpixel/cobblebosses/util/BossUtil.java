package com.kingpixel.cobblebosses.util;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import net.minecraft.entity.Entity;

public class BossUtil {
  public static boolean isBossOrHighLevel(PokemonEntity pokemonEntity) {
    Pokemon pokemon = pokemonEntity.getPokemon();
    return pokemon.getLevel() > CobbleBosses.oldLevelCap || pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID);
  }

  public static boolean isBossOrHighLevel(Entity entity) {
    if (entity instanceof PokemonEntity pokemonEntity) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      return pokemon.getLevel() > CobbleBosses.oldLevelCap || pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID);
    }
    return false;
  }
}
