package com.kingpixel.cobblebosses.model;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

public class ParticleEffectManager {

    private final String colorHex;
    private final float minSize;
    private final float maxSize;

    public ParticleEffectManager(String colorHex, float minSize, float maxSize) {
        this.colorHex = colorHex;
        this.minSize = minSize;
        this.maxSize = maxSize;
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
        Vector3f rgbColor = hexToRGB(colorHex);

        if (bossEntity instanceof PokemonEntity pokemonEntity) {

            float width = pokemonEntity.getPokemon().getSpecies().getHitbox().width();
            float height = pokemonEntity.getPokemon().getSpecies().getHitbox().height();

            int minParticles = 1;
            int maxParticles = 5;
            float scaleFactor = 5.0f;

            int particleCount = (int) (minParticles + (width + height) * scaleFactor / 4f); // Particle count calculation probably unnecessary

            if ((width + height) / 2.5f <= 1.5f) {
                particleCount = (int) (particleCount * 0.5f);
            }

            particleCount = Math.max(minParticles, Math.min(particleCount, maxParticles)); // Limits Particles between specified amount

            float baseSpread = 0.2f;
            float spreadX = baseSpread + (width * 0.4f);
            float spreadZ = baseSpread + (width * 0.4f);
            float spreadY = baseSpread + (height * 0.4f); // Calculates area of particles ie; width, height

            if (width > 2.0f) {
                spreadX *= 1.5f;
                spreadZ *= 1.5f;
            }
            if (height > 2.0f) {
                spreadY *= 1.5f; // Makes sure larger Pokemon have a slightly bigger spread than others (Might need tweaking slightly)
            }
            float maxSpread = 2.5f;
            float maxHeightSpread = 3.0f;
            spreadX = Math.min(spreadX, maxSpread);
            spreadY = Math.min(spreadY, maxHeightSpread);
            spreadZ = Math.min(spreadZ, maxSpread); // Specify the max spread of particles

            float particleSize = Math.max(minSize, Math.min((width + height) / 5.0f, maxSize));

            DustParticleEffect particleEffect = new DustParticleEffect(rgbColor, particleSize);
            spawnParticlesContinuously(world, pokemonEntity, particleEffect, spreadX, spreadY, spreadZ, particleCount, height);
        }
    }


    private void spawnParticlesContinuously(ServerWorld world, LivingEntity bossEntity, DustParticleEffect particleEffect, float spreadX, float spreadY, float spreadZ, int particleCount, float height) {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (bossEntity.isAlive()) {
                double centerX = bossEntity.getX();
                double centerY = bossEntity.getY() + height / 2.0;
                double centerZ = bossEntity.getZ(); // Sets particles to be centered on the Pokemons hit box

                world.spawnParticles(particleEffect,
                        centerX, centerY, centerZ,
                        particleCount, spreadX, spreadY, spreadZ, 1);
            }
        });
    }
}
