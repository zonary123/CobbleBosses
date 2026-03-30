package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.util.BossUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PreventDeathCrash {
  @Inject(method = "onDeath", at = @At("HEAD"))
  private void cobbleBosses$onDeath(DamageSource source, CallbackInfo ci) {
    LivingEntity livingEntity = (LivingEntity) (Object) this;
    if (livingEntity instanceof PokemonEntity pokemonEntity) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      if (BossUtil.isBossOrHighLevel(pokemonEntity)) {
        pokemon.setLevel(CobbleBosses.oldLevelCap);
      }
    }
  }
}
