package com.kingpixel.cobblebosses.util;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import net.minecraft.entity.Entity;

public class BossUtil {
  public static boolean isBoss(PokemonEntity pokemonEntity) {
    if (pokemonEntity == null) return false;
    Pokemon pokemon = pokemonEntity.getPokemon();
    return pokemon != null && pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID);
  }

  public static boolean isBoss(Entity entity) {
    if (entity instanceof PokemonEntity pokemonEntity) {
      return isBoss(pokemonEntity);
    }
    return false;
  }

  public static boolean isBossOrHighLevel(PokemonEntity pokemonEntity) {
    if (pokemonEntity == null) return false;
    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon == null) return false;
    return pokemon.getLevel() > CobbleBosses.oldLevelCap || pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID);
  }

  public static boolean isBossOrHighLevel(Entity entity) {
    if (entity instanceof PokemonEntity pokemonEntity) {
      return isBossOrHighLevel(pokemonEntity);
    }
    return false;
  }
}
