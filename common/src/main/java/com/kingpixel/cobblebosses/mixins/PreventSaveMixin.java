package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 14/02/2025 5:51
 */
@Mixin(Entity.class)
public class PreventSaveMixin {
  @Inject(method = "saveNbt", at = @At("HEAD"), cancellable = true)
  private void PreventSaveMixin$saveNbt(CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    switch (entity) {
      case null -> {
        return;
      }
      case PokemonEntity pokemonEntity -> {
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon == null) return;
        if (pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) {
          cir.setReturnValue(false);
        }
      }
      case BoatEntity boatEntity -> {
        var passenger = boatEntity.getControllingPassenger();
        if (passenger == null) {
          if (CobbleBosses.config.isDebug()) {
            CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Boat without passenger");
          }
          return;
        }
        if (CobbleBosses.config.isDebug()) {
          CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Boat passenger: " + passenger.getSavedEntityId());
        }
        if (passenger instanceof PokemonEntity pokemonEntity) {
          Pokemon pokemon = pokemonEntity.getPokemon();
          if (pokemon == null) return;
          if (pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) {
            cir.setReturnValue(false);
          }
        }
      }
      default -> {
      }
    }
  }
}
