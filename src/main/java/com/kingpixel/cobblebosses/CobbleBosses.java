package com.kingpixel.cobblebosses;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.command.CommandTree;
import com.kingpixel.cobblebosses.config.BossesConfig;
import com.kingpixel.cobblebosses.config.Config;
import com.kingpixel.cobblebosses.config.Lang;
import com.kingpixel.cobblebosses.events.BattleEvents;
import com.kingpixel.cobblebosses.events.CaptureEvents;
import com.kingpixel.cobblebosses.events.SpawningEvents;
import com.kingpixel.cobblebosses.events.StartBattleEvent;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.util.BossTimerManager;
import com.kingpixel.cobbleutils.util.UtilsFile;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class CobbleBosses implements ModInitializer {
  public static final String MOD_ID = "cobblebosses";
  public static final String MOD_NAME = "CobbleBosses";
  public static final String PATH = "/config/" + MOD_ID;
  public static final String PATH_LANG = PATH + "/lang/";
  public static final String PATH_BOSSES = PATH + "/bosses/";
  public static final String TAG_BOSS_ID = "boss";
  public static final String TAG_OWNER_UUID = "boss_owner_uuid";
  public static final String TAG_OWNER_NAME = "boss_owner_name";
  public static final String TAG_DESPAWN_TIME = "boss_despawn_time";
  public static MinecraftServer server;
  public static Config config = new Config();
  public static Lang language = new Lang();
  public static BossesConfig bossesConfig = new BossesConfig();
  public static int oldLevelCap = 100;
  public static int maxLevelCap = 0;


  @Override public void onInitialize() {
    events();
  }

  public static void load() {
    files();
    tasks();
  }

  private static void tasks() {
    BossTimerManager.init();
  }


  private static void files() {
    config.init();
    language.init();
    bossesConfig.init();
    UtilsFile.extractWiki(CobbleBosses.class, Path.of("config", MOD_ID));
  }


  private static void events() {
    files();


    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
      CommandTree.register(dispatcher, registry);
    });

    LifecycleEvent.SERVER_STARTED.register(server -> {
      load();
      oldLevelCap = Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel();
    });


    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    SpawningEvents.register();
    BattleEvents.register();
    CaptureEvents.register();
    StartBattleEvent.register();

    ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
      if (entity instanceof PokemonEntity pokemonEntity) {
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon == null) return;
        NbtCompound nbt = pokemon.getPersistentData();
        if (!nbt.contains(TAG_BOSS_ID)) return;
        String id = nbt.getString(TAG_BOSS_ID);
        Boss boss = bossesConfig.getBoss(id);
        if (boss == null) return;

        if (nbt.contains(TAG_DESPAWN_TIME)) {
          long despawnTime = nbt.getLong(TAG_DESPAWN_TIME);
          if (System.currentTimeMillis() >= despawnTime) {
            boss.removeBossFromTeam(world, pokemonEntity);
            CobbleBosses.server.executeSync(() -> pokemonEntity.remove(Entity.RemovalReason.DISCARDED));
            return;
          }
          BossTimerManager.track(pokemonEntity);
        }

        if (nbt.contains("boss_real_level")) {
          int realLevel = nbt.getInt("boss_real_level");
          if (realLevel > oldLevelCap) {
            Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(maxLevelCap);
            pokemon.setLevel(realLevel);
            Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(oldLevelCap);
          }
        }
        boss.applyBossEffects(world, pokemonEntity);
      }
    });
  }
}
