package com.kingpixel.cobblebosses.config;

import com.google.gson.Gson;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class Lang {
  private String reload;
  private String youCanCatch;
  private String youCanFight;
  private String moveBanned;
  private String cannotFightYet;
  private String notYourBoss;
  private String bossSpawnedReward;
  private String bossSpawnedSuccess;
  private String bossTimeout;
  private String bossTimeoutWarning;

  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public Lang() {
    this.reload = "%prefix% &7Reloaded &7.";
    this.youCanCatch = "%prefix% &aNow you can catch the boss.";
    this.youCanFight = "%prefix% &aNow you can fight the boss.";
    this.moveBanned = "%prefix% &aThe pokemon in the slot %slot% has a banned move and cannot be used in battle.";
    this.cannotFightYet = "%prefix% &cYou must lower the boss's health before you can battle it.";
    this.notYourBoss = "%prefix% &cThis boss was spawned for %owner% and only they can fight it!";
    this.bossSpawnedReward = "%prefix% &aA boss &e%boss% &ahas appeared for you! You have &e%time% &ato defeat it.";
    this.bossSpawnedSuccess = "%prefix% &aBoss &e%boss% &aspawned for &e%player% &awith a timer of &e%time%&a.";
    this.bossTimeout = "%prefix% &cYour boss timed out and disappeared!";
    this.bossTimeoutWarning = "%prefix% &eWarning: Your boss will disappear in &c%time%&e!";
  }

  public String getCannotFightYet() {
    if (cannotFightYet == null) {
      cannotFightYet = "%prefix% &cYou must lower the boss's health before you can battle it.";
    }
    return cannotFightYet;
  }

  public String getNotYourBoss() {
    if (notYourBoss == null) {
      notYourBoss = "%prefix% &cThis boss was spawned for %owner% and only they can fight it!";
    }
    return notYourBoss;
  }

  public String getBossSpawnedReward() {
    if (bossSpawnedReward == null) {
      bossSpawnedReward = "%prefix% &aA boss &e%boss% &ahas appeared for you! You have &e%time% &ato defeat it.";
    }
    return bossSpawnedReward;
  }

  public String getBossSpawnedSuccess() {
    if (bossSpawnedSuccess == null) {
      bossSpawnedSuccess = "%prefix% &aBoss &e%boss% &aspawned for &e%player% &awith a timer of &e%time%&a.";
    }
    return bossSpawnedSuccess;
  }

  public String getBossTimeout() {
    if (bossTimeout == null) {
      bossTimeout = "%prefix% &cYour boss timed out and disappeared!";
    }
    return bossTimeout;
  }

  public String getBossTimeoutWarning() {
    if (bossTimeoutWarning == null) {
      bossTimeoutWarning = "%prefix% &eWarning: Your boss will disappear in &c%time%&e!";
    }
    return bossTimeoutWarning;
  }

  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleBosses.PATH_LANG, CobbleBosses.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        CobbleBosses.language = gson.fromJson(el, Lang.class);
        String data = gson.toJson(CobbleBosses.language);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleBosses.PATH_LANG, CobbleBosses.config.getLang() +
            ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal(CobbleBosses.MOD_ID, "Could not write lang.json file for " + CobbleBosses.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "No lang.json file found for" + CobbleBosses.MOD_NAME + ". Attempting " +
        "to generate one.");
      Gson gson = Utils.newGson();
      CobbleBosses.language = this;
      String data = gson.toJson(CobbleBosses.language);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleBosses.PATH_LANG, CobbleBosses.config.getLang() +
          ".json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal(CobbleBosses.MOD_ID, "Could not write lang.json file for " + CobbleBosses.MOD_NAME + ".");
      }
    }
  }


}
