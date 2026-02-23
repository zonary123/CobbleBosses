package com.kingpixel.cobblebosses.model;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.Data;

@Data
public class Damageable {
  private boolean enabled;
  private Double untilLifePercentage;
  private boolean catchable;
  private int level;

  public Damageable() {
    this.enabled = false;
    this.untilLifePercentage = 10.0D;
    this.catchable = false;
    this.level = 1;
  }

  public boolean isDownLife(PokemonEntity pokemonEntity) {
    Pokemon pokemon = pokemonEntity.getPokemon();
    float percentageOfLife = (100 * pokemonEntity.getHealth() / pokemon.getMaxHealth());
    return percentageOfLife > untilLifePercentage;
  }
}
