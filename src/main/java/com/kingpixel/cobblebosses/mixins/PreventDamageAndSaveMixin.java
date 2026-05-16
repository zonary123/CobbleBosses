package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.model.Damageable;
import com.kingpixel.cobblebosses.util.BossUtil;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.util.UUID;

@Mixin(PokemonEntity.class)
public abstract class PreventDamageAndSaveMixin {

  @Unique
  private static final Cache<UUID, Boolean> rewardCooldown =
    Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofSeconds(3))
      .build();

  @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
  private void cobbleBosses$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

    PokemonEntity entity = (PokemonEntity) (Object) this;
    Pokemon pokemon = entity.getPokemon();

    if (!pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) return;

    Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
    if (boss == null) return;

    Damageable damageable = boss.getDamageable();

    if (damageable == null || !damageable.isEnabled()) {
      cir.setReturnValue(false);
      cir.cancel();
      return;
    }

    if (damageable.isCatchable() && !pokemon.isUncatchable()) {
      cir.setReturnValue(false);
      cir.cancel();
      return;
    }

    Entity attacker = source.getAttacker();

    if (!(attacker instanceof ServerPlayerEntity player)) {
      cir.setReturnValue(false);
      cir.cancel();
      return;
    }

    float currentHealth = entity.getHealth();
    float predictedHealth = currentHealth - amount;

    if (predictedHealth <= 0.0F) {

      entity.setHealth(1.0F);

      if (damageable.isCatchable()) {

        PokemonProperties.Companion
          .parse("uncatchable=no")
          .apply(pokemon);

        PlayerUtils.sendMessage(
          player.getUuid(),
          CobbleBosses.language.getYouCanCatch(),
          CobbleBosses.config.getPrefix(),
          TypeMessage.CHAT
        );

      } else {

        PlayerUtils.sendMessage(
          player.getUuid(),
          CobbleBosses.language.getYouCanFight(),
          CobbleBosses.config.getPrefix(),
          TypeMessage.CHAT
        );
      }

      if (rewardCooldown.getIfPresent(player.getUuid()) == null) {

        rewardCooldown.put(player.getUuid(), true);

        AdvancedItemChance rewards = boss.getRewards();

        if (rewards != null) {
          rewards.openMenu(player, t -> {
          }, c -> {
          });
        }
      }

      cir.setReturnValue(false);
      cir.cancel();
    }
  }

  @Inject(method = "shouldSave", at = @At("HEAD"), cancellable = true)
  private void cobbleBosses$shouldSave(CallbackInfoReturnable<Boolean> cir) {

    PokemonEntity entity = (PokemonEntity) (Object) this;

    if (BossUtil.isBossOrHighLevel(entity)) {
      cir.setReturnValue(false);
      cir.cancel();
    }
  }

  @Inject(method = "canStartRiding", at = @At("HEAD"), cancellable = true)
  private void canStartRiding(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    handleRideCancel(passenger, cir);
  }

  @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
  private void canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    handleRideCancel(passenger, cir);
  }

  @Unique
  private void handleRideCancel(Entity passenger, CallbackInfoReturnable<Boolean> cir) {

    Entity self = (Entity) (Object) this;

    if (BossUtil.isBossOrHighLevel(self) || BossUtil.isBossOrHighLevel(passenger)) {
      cir.setReturnValue(false);
      cir.cancel();
    }
  }
}