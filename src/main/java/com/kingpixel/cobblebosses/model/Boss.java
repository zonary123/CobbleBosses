package com.kingpixel.cobblebosses.model;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import kotlin.Unit;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import com.kingpixel.cobblebosses.util.BossTimerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

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
  private List<String> pokemons;
  private String properties;
  private AdvancedItemChance rewards;
  private Damageable damageable;

  public Boss() {
    id = "default";
    nickName = "§e%pokemon% §9Boss";
    glowing = true;
    glowingColor = Formatting.LIGHT_PURPLE;
    particles = true;
    particleColor = "#CBC3E3";
    chance = 0.1f;
    maxLevel = 120;
    minLevel = 100;
    maxSize = 2.0f;
    minSize = 1.5f;
    pokemons = List.of("pikachu");
    properties = "shiny=true";
    rewards = new AdvancedItemChance();
    damageable = new Damageable();
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

    if (particleColor == null) particleColor = "#CBC3E3";
    if (glowingColor == null) glowingColor = Formatting.LIGHT_PURPLE;
    if (nickName == null) nickName = "§e%pokemon% §9Boss";
    if (properties == null) properties = "shiny=true";
    if (rewards == null) rewards = new AdvancedItemChance();
    if (damageable == null) damageable = new Damageable();
  }

  public void convert(PokemonEntity p) {
    ServerWorld world = (ServerWorld) p.getEntityWorld();
    Vec3d pos = p.getPos();
    CobbleBosses.server.executeSync(() -> p.remove(Entity.RemovalReason.DISCARDED));
    Pokemon pokemon = p.getPokemon().clone(true, CobbleUtils.server.getRegistryManager());
    spawn(world, pos, pokemon);
  }

  private static final String TEAM_PREFIX = "boss_";

  public void assignBossToTeam(ServerWorld world, LivingEntity bossEntity) {
    if (bossEntity == null || world == null) {
      return;
    }
    Scoreboard scoreboard = world.getScoreboard();
    String teamColorName = TEAM_PREFIX + this.glowingColor.getName();
    Team team = scoreboard.getTeam(teamColorName);

    if (team == null) {
      team = scoreboard.addTeam(teamColorName);
      team.setDisplayName(Text.literal(TEAM_PREFIX + this.glowingColor.getName()));
      team.setFriendlyFireAllowed(false);
    }
    team.setColor(this.glowingColor);
    scoreboard.addScoreHolderToTeam(bossEntity.getNameForScoreboard(), team);
  }

  public void removeBossFromTeam(ServerWorld world, LivingEntity bossEntity) {
    if (bossEntity == null || world == null) {
      return;
    }
    Scoreboard scoreboard = world.getScoreboard();
    String teamColorName = TEAM_PREFIX + this.glowingColor.getName();
    Team team = scoreboard.getTeam(teamColorName);
    if (team != null) {
      scoreboard.removeScoreHolderFromTeam(bossEntity.getNameForScoreboard(), team);
    }
  }

  public void applyBossEffects(ServerWorld world, PokemonEntity bossEntity) {
    if (bossEntity == null || world == null) return;
    Pokemon pokemon = bossEntity.getPokemon();
    if (pokemon == null) return;
    bossEntity.setPersistent();

    if (glowing) {
      bossEntity.setGlowing(true);
      bossEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, -1, 0, false, false));
      assignBossToTeam(world, bossEntity);
    }

    bossEntity.setDrops(new DropTable());

    if (particles && !particleColor.isEmpty()) {
      ParticleEffectManager particleEffectManager = new ParticleEffectManager(particleColor);
      particleEffectManager.spawnParticles(world, bossEntity);
    }

    String pokemonName = pokemon.getSpecies() != null ? pokemon.getSpecies().getTranslatedName().getString() : "";
    var text = AdventureTranslator.toNativeComponent(nickName.replace("%pokemon%", pokemonName));
    bossEntity.setCustomNameVisible(true);
    pokemon.setNickname(text);
    bossEntity.setCustomName(text);
  }

  public void spawn(ServerWorld world, Vec3d pos, Pokemon pokemon) {
    spawn(world, pos, pokemon, null, 0);
  }

  public void spawn(ServerWorld world, Vec3d pos, Pokemon pokemon, ServerPlayerEntity owner, int durationSeconds) {
    if (pokemons.isEmpty()) {
      PokemonProperties.Companion.parse("uncatchable=true " + getProperties()).apply(pokemon);
    } else {
      String pokemonId = pokemons.get(Utils.getRandom().nextInt(pokemons.size()));
      pokemon = PokemonProperties.Companion.parse(pokemonId + " uncatchable=true " + getProperties()).create();
    }

    if (minSize == maxSize) {
      pokemon.setScaleModifier(maxSize);
    } else {
      pokemon.setScaleModifier(Utils.getRandom().nextFloat(minSize, maxSize));
    }

    Pokemon finalPokemon = pokemon;
    CobbleBosses.server.execute(() -> {
      NbtCompound nbt = finalPokemon.getPersistentData();
      nbt.putString(CobbleBosses.TAG_BOSS_ID, id);
      int targetLevel = (minLevel == maxLevel) ? maxLevel : Utils.getRandom().nextInt(minLevel, maxLevel);
      nbt.putInt("boss_real_level", targetLevel);

      if (owner != null) {
        nbt.putString(CobbleBosses.TAG_OWNER_UUID, owner.getUuid().toString());
        nbt.putString(CobbleBosses.TAG_OWNER_NAME, owner.getGameProfile().getName());
      }

      if (durationSeconds > 0) {
        long despawnTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        nbt.putLong(CobbleBosses.TAG_DESPAWN_TIME, despawnTime);
      }

      Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(CobbleBosses.maxLevelCap);
      finalPokemon.setLevel(targetLevel);
      Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(CobbleBosses.oldLevelCap);

      finalPokemon.sendOut(world, pos, null, bossEntity -> {
        applyBossEffects(world, bossEntity);
        if (durationSeconds > 0) {
          BossTimerManager.track(bossEntity, durationSeconds);
        }
        return Unit.INSTANCE;
      });
    });
  }
}