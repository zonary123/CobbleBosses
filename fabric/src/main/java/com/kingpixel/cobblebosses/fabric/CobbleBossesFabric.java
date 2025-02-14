package com.kingpixel.cobblebosses.fabric;

import com.kingpixel.cobblebosses.CobbleBosses;
import net.fabricmc.api.ModInitializer;

public class CobbleBossesFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    CobbleBosses.init();
  }
}
