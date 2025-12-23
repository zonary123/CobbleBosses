package com.kingpixel.cobblebosses.config;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 17/01/2025 0:26
 */
@Getter
public class BossesConfig {
  public List<Boss> bosses = new ArrayList<>();


  public void init() {
    bosses.clear();
    File folder = Utils.getAbsolutePath(CobbleBosses.PATH_BOSSES);

    var files = Utils.getFiles(folder);
    if (files.isEmpty()) {
      folder.mkdirs();
      createDefaultTags();
    } else {
      for (File file : files) {
        Boss boss = null;
        try {
          boss = Utils.newGson().fromJson(Utils.readFileSync(file), Boss.class);
          boss.setId(file.getName().replace(".json", ""));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        boss.check();
        bosses.add(boss);
        Utils.writeFileAsync(CobbleBosses.PATH_BOSSES, boss.getId() + ".json", Utils.newGson().toJson(boss));
      }
    }

    for (Boss boss : bosses) {
      if (CobbleBosses.maxLevelCap < boss.getMaxLevel()) CobbleBosses.maxLevelCap = boss.getMaxLevel();
    }
  }


  private void createDefaultTags() {
    bosses.add(new Boss());

    bosses.forEach(tag -> {
      Utils.writeFileAsync(CobbleBosses.PATH_BOSSES, tag.getId() + ".json", Utils.newGson().toJson(tag));
    });
  }


  public static Boss getRandomBoss() {
    float totalWeight = 0;
    for (Boss boss : CobbleBosses.bossesConfig.getBosses()) {
      totalWeight += boss.getChance();
    }

    float random = Utils.getRandom().nextFloat() * totalWeight;
    for (Boss boss : CobbleBosses.bossesConfig.getBosses()) {
      random -= boss.getChance();
      if (random <= 0) {
        return boss;
      }
    }
    return null;
  }

  public Boss getBoss(Pokemon pokemon) {
    String id = pokemon.getPersistentData().getString(CobbleBosses.TAG_BOSS_ID);
    for (Boss boss : bosses) {
      if (boss.getId().equals(id)) return boss;
    }
    return null;
  }

  public Boss getBoss(String id) {
    if (id == null) return null;
    for (Boss boss : bosses) {
      if (boss.getId().equals(id)) return boss;
    }
    return null;
  }
}
