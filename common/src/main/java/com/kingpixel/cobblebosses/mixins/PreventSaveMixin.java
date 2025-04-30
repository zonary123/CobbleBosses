package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 14/02/2025 5:51
 */
@Mixin(Entity.class)
public abstract class PreventSaveMixin {
  @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
  private void canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (cobbleBosses$isBossOrPassLevel(entity) || cobbleBosses$isBossOrPassLevel(passenger)) {
      if (CobbleBosses.config.isDebug()) {
        CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Preventing passenger: " + passenger.getSavedEntityId());
      }
      cir.setReturnValue(false);
    }
  }

  @Inject(method = "canStartRiding", at = @At("HEAD"), cancellable = true)
  private void canStartRiding(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (cobbleBosses$isBossOrPassLevel(entity) || cobbleBosses$isBossOrPassLevel(passenger)) {
      if (CobbleBosses.config.isDebug()) {
        CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Preventing passenger: " + passenger.getSavedEntityId());
      }
      cir.cancel();
    }
  }

  @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
  private void startRiding(Entity passenger, boolean force, CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (cobbleBosses$isBossOrPassLevel(entity) || cobbleBosses$isBossOrPassLevel(passenger)) {
      if (CobbleBosses.config.isDebug()) {
        CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Preventing passenger: " + passenger.getSavedEntityId());
      }
      cir.cancel();
    }
  }

  @Inject(method = "canStartRiding", at = @At("HEAD"), cancellable = true)
  private void canBeRidden(CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (cobbleBosses$isBossOrPassLevel(entity)) {
      if (CobbleBosses.config.isDebug()) {
        CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Preventing passenger: " + entity.getSavedEntityId());
      }
      cir.setReturnValue(false);
    }
  }

  @Unique private boolean cobbleBosses$isBossOrPassLevel(Entity entity) {
    if (entity instanceof PokemonEntity pokemonEntity) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      return pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID) || pokemon.getLevel() > Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel();
    }
    return false;
  }

  @Inject(method = "saveNbt", at = @At("HEAD"), cancellable = true)
  private void PreventSaveMixin$saveNbt(CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (entity instanceof PokemonEntity pokemonEntity) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      if (pokemon.getLevel() > Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel()) cir.setReturnValue(false);
    }
  }
}
