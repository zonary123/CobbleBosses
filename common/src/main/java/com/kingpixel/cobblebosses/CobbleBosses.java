package com.kingpixel.cobblebosses;

import com.cobblemon.mod.common.Cobblemon;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kingpixel.cobblebosses.command.CommandTree;
import com.kingpixel.cobblebosses.config.BossesConfig;
import com.kingpixel.cobblebosses.config.Config;
import com.kingpixel.cobblebosses.config.Lang;
import com.kingpixel.cobblebosses.events.BattleEvents;
import com.kingpixel.cobblebosses.events.SpawningEvents;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CobbleBosses {
  public static final String MOD_ID = "cobblebosses";
  public static final String MOD_NAME = "CobbleBosses";
  public static final String PATH = "/config/" + MOD_ID;
  public static final String PATH_LANG = PATH + "/lang/";
  public static final String PATH_BOSSES = PATH + "/bosses/";
  public static final String TAG_BOSS_ID = "boss";
  public static MinecraftServer server;
  public static Config config = new Config();
  public static Lang language = new Lang();
  public static BossesConfig bossesConfig = new BossesConfig();
  public static int oldLevelCap = 100;
  public static int maxLevelCap = 0;
  public static Executor EXECUTOR_COBBLE_BOSSES = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder()
    .setNameFormat("executor-cobblebosses-%d")
    .setDaemon(true)
    .build());

  public static void init() {
    events();
  }

  public static void load() {
    files();
    tasks();
  }

  private static void tasks() {

  }


  private static void files() {
    config.init();
    language.init();
    bossesConfig.init();
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
  }
}
