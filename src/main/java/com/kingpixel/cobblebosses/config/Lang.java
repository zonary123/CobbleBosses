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

  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public Lang() {
    this.reload = "%prefix% &7Reloaded &7.";
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
