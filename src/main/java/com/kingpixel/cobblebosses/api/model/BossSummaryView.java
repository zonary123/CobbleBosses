package com.kingpixel.cobblebosses.api.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Immutable summary view representing a configured Boss template.
 */
@Getter
public class BossSummaryView {

  private final String id;
  private final String nickName;
  private final boolean glowing;
  private final String glowingColor;
  private final boolean particles;
  private final String particleColor;
  private final float chance;
  private final int minLevel;
  private final int maxLevel;
  private final float minSize;
  private final float maxSize;
  private final List<String> pokemons;
  private final String properties;
  private final boolean damageableEnabled;
  private final double untilLifePercentage;
  private final boolean catchable;
  private final int captureLevel;

  public BossSummaryView(String id,
                         String nickName,
                         boolean glowing,
                         String glowingColor,
                         boolean particles,
                         String particleColor,
                         float chance,
                         int minLevel,
                         int maxLevel,
                         float minSize,
                         float maxSize,
                         List<String> pokemons,
                         String properties,
                         boolean damageableEnabled,
                         double untilLifePercentage,
                         boolean catchable,
                         int captureLevel) {
    this.id = id != null ? id : "";
    this.nickName = nickName != null ? nickName : "";
    this.glowing = glowing;
    this.glowingColor = glowingColor != null ? glowingColor : "";
    this.particles = particles;
    this.particleColor = particleColor != null ? particleColor : "";
    this.chance = chance;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.minSize = minSize;
    this.maxSize = maxSize;
    this.pokemons = pokemons != null ? Collections.unmodifiableList(List.copyOf(pokemons)) : Collections.emptyList();
    this.properties = properties != null ? properties : "";
    this.damageableEnabled = damageableEnabled;
    this.untilLifePercentage = untilLifePercentage;
    this.catchable = catchable;
    this.captureLevel = captureLevel;
  }
}
