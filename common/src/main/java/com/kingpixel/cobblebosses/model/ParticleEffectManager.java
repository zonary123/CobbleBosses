package com.kingpixel.cobblebosses.model;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

public class ParticleEffectManager {
  private final String particleColor;

  public ParticleEffectManager(String colorHex) {
    this.particleColor = colorHex;
  }

  public Vector3f hexToRGB(String hex) {
    if (hex != null && hex.startsWith("#") && hex.length() == 7) {
      try {
        int color = Integer.parseInt(hex.substring(1), 16);
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        return new Vector3f(red, green, blue);
      } catch (NumberFormatException e) {
        return new Vector3f(1.0f, 1.0f, 1.0f);
      }
    }
    return new Vector3f(1.0f, 1.0f, 1.0f);
  }

  public void spawnParticles(ServerWorld world, LivingEntity bossEntity) {
    Vector3f rgbColor = hexToRGB(particleColor);

    if (!(bossEntity instanceof PokemonEntity pokemonEntity)) {
      return;
    }

    var hitbox = pokemonEntity.getPokemon().getForm().getHitbox();

    float width = hitbox.width();
    float height = hitbox.height();


    int particleCount = 5;

    float[] spreads = calculateParticleSpread(pokemonEntity, width, height);
    float spreadX = spreads[0], spreadY = spreads[1], spreadZ = spreads[2];

    float particleSize = 1;

    DustParticleEffect particleEffect = new DustParticleEffect(rgbColor, particleSize);
    scheduleParticleTask(world, bossEntity, particleEffect, spreadX, spreadY, spreadZ, particleCount, height);
  }

  private float[] calculateParticleSpread(PokemonEntity pokemonEntity, float width, float height) {
    float scale = pokemonEntity.getScale();
    float spreadX = Math.max(0.5f, Math.min(width * 0.4f, 2.5f)) * scale;
    float spreadZ = Math.max(0.5f, Math.min(width * 0.4f, 2.5f)) * scale;
    float spreadY = (height) * scale;

    return new float[]{spreadX, spreadY, spreadZ};
  }

  private long calculateDynamicIntervalInTicks(float width, float height) {
    long minInterval = 50;
    long maxInterval = 400;
    float sizeFactor = (width + height) / 2.0f;
    float scaleFactor = Math.max(0.1f, Math.min(sizeFactor / 3.0f, 1.0f));
    long intervalMillis = (long) (maxInterval - (scaleFactor * (maxInterval - minInterval)));
    long intervalTicks = intervalMillis / 50; // Convert milliseconds to ticks (1 tick = 50 ms)
    return Math.max(1, intervalTicks); // Ensure at least 1 tick
  }

  private void scheduleParticleTask(ServerWorld world, LivingEntity bossEntity,
                                    DustParticleEffect particleEffect, float spreadX, float spreadY, float spreadZ,
                                    int particleCount, float height) {

    long interval = calculateDynamicIntervalInTicks(bossEntity.getWidth(), bossEntity.getHeight());

    Task.builder()
      .execute((task) -> {
        if (bossEntity == null || !bossEntity.isAlive()) {
          task.setExpired();
          return;
        }

        double centerX = bossEntity.getX();
        double centerY = bossEntity.getY() + (height / 2.0);
        double centerZ = bossEntity.getZ();

        world.spawnParticles(particleEffect, centerX, centerY, centerZ, particleCount, spreadX, spreadY, spreadZ, 1);
      })
      .interval(interval)
      .infinite()
      .build();

  }
}
