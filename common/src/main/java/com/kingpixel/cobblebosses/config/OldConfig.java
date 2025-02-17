package com.kingpixel.cobblebosses.config;

/**
 * @author Carlos Varas Alonso - 17/02/2025 22:14
 */

import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.Model.Particle;
import com.kingpixel.cobbleutils.Model.Sound;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 11/12/2024 21:13
 */
@Getter
@Setter
public class OldConfig {
  private boolean active;
  private boolean shiny;
  private String rarity;
  private String pokemonData;
  private String nickname;
  private boolean glowing;
  private Formatting glowingColor;
  private float chance;
  private int minlevel;
  private int maxlevel;
  private float minsize;
  private float maxsize;
  private Sound sound;
  private Particle particle;
  private List<String> pokemons;
  private List<String> blacklist;
  private AdvancedItemChance rewards;

  public static void migrate() {
    File folder = Utils.getAbsolutePath("/config/cobbleutils/boss/");

    if (!folder.exists()) {
      CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "No old bosses found.");
      return;
    }
    File[] files = folder.listFiles();
    if (files == null) {
      CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "No old bosses found.");
      return;
    }
    for (File file : files) {
      Utils.readFileSync(file, call -> {
        OldConfig oldConfig = Utils.newGson().fromJson(call, OldConfig.class);
        Boss boss = new Boss();
        boss.setId(file.getName().replace(".json", ""));
        boss.setChance(oldConfig.getChance());
        boss.setMinLevel(oldConfig.getMinlevel());
        boss.setMaxLevel(oldConfig.getMaxlevel());
        boss.setMinSize(oldConfig.getMinsize());
        boss.setMaxSize(oldConfig.getMaxsize());
        boss.setParticles(true);
        boss.setGlowing(oldConfig.isGlowing());
        boss.setGlowingColor(oldConfig.getGlowingColor());
        boss.setNickName(oldConfig.getNickname());
        boss.setRewards(oldConfig.getRewards());
        boss.setParticleColor("#FFFFFF");
        Utils.writeFileAsync(CobbleBosses.PATH_BOSSES, boss.getId() + ".json", Utils.newGson().toJson(boss));
        CobbleBosses.bossesConfig.getBosses().add(boss);
      });
    }
    boolean delete = folder.delete();
    if (!delete) {
      CobbleUtils.LOGGER.fatal(CobbleBosses.MOD_ID, "Could not delete old bosses folder.");
      folder.deleteOnExit();
    } else {
      CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Old bosses folder deleted.");
    }
    CobbleBosses.load();
  }
}
