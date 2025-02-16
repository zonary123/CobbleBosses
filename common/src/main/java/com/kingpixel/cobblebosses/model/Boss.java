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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
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
  private boolean glowing;
  private Formatting glowingColor;
  private boolean particles;
  private String particleColor;
  private float chance;
  private int maxLevel;
  private int minLevel;
  private float maxSize;
  private float minSize;
  private AdvancedItemChance rewards;

  public Boss() {
    id = "default";
    nickName = "§e%pokemon% §9Boss";
    glowing = true;
    glowingColor = Formatting.DARK_PURPLE;
    particles = true;
    particleColor = "#FF5733";
    chance = 0.1f;
    maxLevel = 120;
    minLevel = 100;
    maxSize = 2.0f;
    minSize = 1.5f;
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

    if (particleColor == null) particleColor = "#FF5733";
    if (glowingColor == null) glowingColor = Formatting.DARK_PURPLE;
  }

  public void convert(PokemonEntity p) {
    Pokemon pokemon = p.getPokemon().clone(true, DynamicRegistryManager.EMPTY);
    ServerWorld world = (ServerWorld) p.getEntityWorld();
    Vec3d pos = p.getPos();
    p.remove(Entity.RemovalReason.DISCARDED);
    spawn(world, pos, pokemon);
  }

  private void assignBossToTeam(ServerWorld world, LivingEntity bossEntity) {
    if (bossEntity == null || world == null) {
      return;
    }
    Scoreboard scoreboard = world.getScoreboard();
    String teamColorName = "boss_" + this.glowingColor.getName();
    Team team = scoreboard.getTeam(teamColorName);

    if (team == null) {
      team = scoreboard.addTeam(teamColorName);
      team.setDisplayName(Text.literal("boss_" + this.glowingColor.getName()));
      team.setFriendlyFireAllowed(false);
    }
    team.setColor(this.glowingColor);
    scoreboard.addScoreHolderToTeam(bossEntity.getNameForScoreboard(), team);

  }

  public void spawn(ServerWorld world, Vec3d pos, Pokemon pokemon) {
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
      if (glowing) {
        bossEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, -1, 0, false, false));
        assignBossToTeam(world, bossEntity);
      }

      if (particles && !particleColor.isEmpty()) {
        ParticleEffectManager particleEffectManager = new ParticleEffectManager(particleColor);
        particleEffectManager.spawnParticles(world, bossEntity);
      }

      var text = Text.empty().append(nickName.replace("%pokemon%", pokemon.getDisplayName().getString()));
      bossEntity.setCustomNameVisible(true);
      bossEntity.getPokemon().setNickname(text);
      bossEntity.setCustomName(text);

      return Unit.INSTANCE;
    });
  }
}