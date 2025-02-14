package com.kingpixel.cobblebosses.model;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.util.Utils;
import kotlin.Unit;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

/**
 * @author Carlos Varas Alonso - 14/02/2025 4:15
 */
@Data
public class Boss {
  private String id;
  private String nickName;
  private float chance;
  private int maxLevel;
  private int minLevel;
  private float maxSize;
  private float minSize;
  private Formatting color;
  private AdvancedItemChance rewards;

  public Boss() {
    id = "default";
    nickName = "§e%pokemon% §9Boss";
    chance = 0.1f;
    maxLevel = 120;
    minLevel = 100;
    maxSize = 2.0f;
    minSize = 1.5f;
    color = Formatting.RED;
    rewards = new AdvancedItemChance();
  }

  public void check() {
    if (minLevel > maxLevel) {
      int temp = minLevel;
      minLevel = maxLevel;
      maxLevel = temp;
    }

    if (minSize > maxSize) {
      float temp = minSize;
      minSize = maxSize;
      maxSize = temp;
    }
  }

  public void convert(PokemonEntity p) {
    Pokemon pokemon = p.getPokemon().clone(true, DynamicRegistryManager.EMPTY);
    ServerWorld world = (ServerWorld) p.getEntityWorld();
    Vec3d pos = p.getPos();
    p.remove(Entity.RemovalReason.DISCARDED);

    PokemonProperties.Companion.parse("uncatchable=true").apply(pokemon);

    if (minSize == maxSize) {
      pokemon.setScaleModifier(maxSize);
    } else {
      pokemon.setScaleModifier(Utils.RANDOM.nextFloat(minSize, maxSize));
    }

    NbtCompound nbt = pokemon.getPersistentData();
    nbt.putString(CobbleBosses.TAG_BOSS_ID, id);

    Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(CobbleBosses.maxLevelCap);
    if (minLevel == maxLevel) {
      pokemon.setLevel(maxLevel);
    } else {
      pokemon.setLevel(Utils.RANDOM.nextInt(minLevel, maxLevel));
    }
    Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(CobbleBosses.oldLevelCap);


    PokemonEntity pokemonEntity = pokemon.sendOut(world, pos, null, bossEntity -> {
      bossEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, -1, 0));

      // Color Glowing

      var text = Text.empty().append(nickName.replace("%pokemon%", pokemon.getDisplayName().getString()));
      bossEntity.setCustomNameVisible(true);
      bossEntity.getPokemon().setNickname(text);
      bossEntity.setCustomName(text);
      return Unit.INSTANCE;
    });


  }
}
