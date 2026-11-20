package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.api.ui.BossMenuRegistry;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.model.Damageable;
import com.kingpixel.cobblebosses.util.BossTimerManager;
import com.kingpixel.cobblebosses.util.BossUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    Entity attacker = source.getAttacker();

    if (!(attacker instanceof ServerPlayerEntity player)) {
      cir.setReturnValue(false);
      cir.cancel();
      return;
    }

    NbtCompound nbt = pokemon.getPersistentData();
    if (nbt.contains(CobbleBosses.TAG_OWNER_UUID)) {
      String ownerUuidStr = nbt.getString(CobbleBosses.TAG_OWNER_UUID);
      if (!player.getUuid().toString().equalsIgnoreCase(ownerUuidStr)) {
        String ownerName = nbt.getString(CobbleBosses.TAG_OWNER_NAME);
        PlayerUtils.sendMessage(
          player.getUuid(),
          CobbleBosses.language.getNotYourBoss()
            .replace("%owner%", ownerName)
            .replace("%player%", player.getGameProfile().getName()),
          CobbleBosses.config.getPrefix(),
          TypeMessage.CHAT
        );
        cir.setReturnValue(false);
        cir.cancel();
        return;
      }
    }


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


    float currentHealth = entity.getHealth();
    float predictedHealth = currentHealth - amount;

    float currentPercentage = (100 * currentHealth / pokemon.getMaxHealth());
    float predictedPercentage = (100 * Math.max(0, predictedHealth) / pokemon.getMaxHealth());
    if (currentPercentage > damageable.getUntilLifePercentage() && predictedPercentage <= damageable.getUntilLifePercentage()) {
      PlayerUtils.sendMessage(
        player.getUuid(),
        CobbleBosses.language.getYouCanFight(),
        CobbleBosses.config.getPrefix(),
        TypeMessage.CHAT
      );
    }

    if (predictedHealth <= 1.0F) {
      if (damageable.isCatchable()) {
        entity.setHealth(1.0F);
        PokemonProperties.Companion
          .parse("uncatchable=no")
          .apply(pokemon);

        PlayerUtils.sendMessage(
          player.getUuid(),
          CobbleBosses.language.getYouCanCatch(),
          CobbleBosses.config.getPrefix(),
          TypeMessage.CHAT
        );
        openRewardMenu(player, boss);
        cir.setReturnValue(false);
        cir.cancel();
      } else {
        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
          boss.removeBossFromTeam(serverWorld, entity);
        }
        boss.getRewards().giveRewards(player);
        BossTimerManager.untrack(entity);
        CobbleBosses.server.executeSync(() -> entity.remove(Entity.RemovalReason.KILLED));
        cir.setReturnValue(true);
        cir.cancel();
      }
    }
  }

  private void openRewardMenu(ServerPlayerEntity player, Boss boss) {
    if (rewardCooldown.getIfPresent(player.getUuid()) == null) {
      rewardCooldown.put(player.getUuid(), true);
      BossMenuRegistry.openBossRewardMenu(player, boss);
    }
  }

  @Inject(method = "shouldSave", at = @At("HEAD"), cancellable = true)
  private void cobbleBosses$shouldSave(CallbackInfoReturnable<Boolean> cir) {
    PokemonEntity entity = (PokemonEntity) (Object) this;
    if (BossUtil.isBoss(entity)) {
      cir.setReturnValue(true);
      cir.cancel();
      return;
    }
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