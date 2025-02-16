package com.kingpixel.cobblebosses.model;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParticleEffectManager {
    private final String particleColor;
    private ScheduledFuture<?> task;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ParticleEffectManager(String colorHex, float minSize, float maxSize) {
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

        float width = pokemonEntity.getPokemon().getSpecies().getHitbox().width();
        float height = pokemonEntity.getPokemon().getSpecies().getHitbox().height();


        int particleCount = 5;

        float[] spreads = calculateParticleSpread(width, height);
        float spreadX = spreads[0], spreadY = spreads[1], spreadZ = spreads[2];

        float particleSize = 1;

        DustParticleEffect particleEffect = new DustParticleEffect(rgbColor, particleSize);
        scheduleParticleTask(world.getServer(), world, bossEntity, particleEffect, spreadX, spreadY, spreadZ, particleCount, height);
    }

    private float[] calculateParticleSpread(float width, float height) {
        float spreadX = Math.max(0.5f, Math.min(width * 0.4f, 2.5f));
        float spreadZ = Math.max(0.5f, Math.min(width * 0.4f, 2.5f));
        float spreadY = Math.min(height * 0.8f, height + 1.0f);
        spreadY = Math.max(spreadY, height * 0.6f);

        return new float[]{spreadX, spreadY, spreadZ};
    }

    private long calculateDynamicInterval(float width, float height) {
        long minInterval = 50;
        long maxInterval = 400;
        float sizeFactor = (width + height) / 2.0f;
        float scaleFactor = Math.max(0.1f, Math.min(sizeFactor / 3.0f, 1.0f));
        long interval = (long) (maxInterval - (scaleFactor * (maxInterval - minInterval)));
        return Math.max(minInterval, Math.min(interval, maxInterval));
    }

    private void scheduleParticleTask(MinecraftServer server, ServerWorld world, LivingEntity bossEntity,
                                      DustParticleEffect particleEffect, float spreadX, float spreadY, float spreadZ,
                                      int particleCount, float height) {

        long interval = calculateDynamicInterval(bossEntity.getWidth(), bossEntity.getHeight());

        task = scheduler.scheduleAtFixedRate(() -> {
            if (!bossEntity.isAlive()) {
                if (task != null && !task.isCancelled()) {
                    task.cancel(false);
                }
                return;
            }

            double centerX = bossEntity.getX();
            double centerY = bossEntity.getY() + (height / 2.0);
            double centerZ = bossEntity.getZ();

            server.submit(() -> {
                world.spawnParticles(particleEffect, centerX, centerY, centerZ, particleCount, spreadX, spreadY, spreadZ, 1);
            });

        }, 500, interval, TimeUnit.MILLISECONDS);
    }
}
