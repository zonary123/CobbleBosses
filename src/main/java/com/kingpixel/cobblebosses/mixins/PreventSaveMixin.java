package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 14/02/2025 5:51
 */
@Mixin(Entity.class)
public abstract class PreventSaveMixin {

  @Inject(method = "saveNbt", at = @At("HEAD"), cancellable = true)
  private void PreventSaveMixin$saveNbt(CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (entity instanceof PokemonEntity pokemonEntity) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      if (pokemon.getLevel() > CobbleBosses.oldLevelCap) {
        cir.setReturnValue(false);
        cir.cancel();
      }
    }
  }
}
