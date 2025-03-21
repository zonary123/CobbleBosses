package com.kingpixel.cobblebosses.config;

import com.google.gson.Gson;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
@Data
@ToString
public class Config {
  private boolean debug;
  private String prefix;
  private String lang;
  private List<String> commands;
  private int rateSpawn;
  private List<String> blackListWorlds;

  public Config() {
    debug = false;
    prefix = "§7[§6CobbleBosses§7] ";
    lang = "en";
    commands = List.of("cobblebosses", "bosses");
    rateSpawn = 2048;
    blackListWorlds = List.of("minecraft:world_nether", "minecraft:world_the_end");
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleBosses.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        CobbleBosses.config = gson.fromJson(el, Config.class);
        String data = gson.toJson(CobbleBosses.config);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleBosses.PATH, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal(CobbleBosses.MOD_ID, "Could not write config.json file for " + CobbleBosses.MOD_NAME +
            ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "No config.json file found for" + CobbleBosses.MOD_NAME + ". Attempting" +
        " to generate one.");
      Gson gson = Utils.newGson();
      CobbleBosses.config = this;
      String data = gson.toJson(CobbleBosses.config);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleBosses.PATH, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal(CobbleBosses.MOD_ID, "Could not write config.json file for " + CobbleBosses.MOD_NAME + ".");
      }
    }

  }
}