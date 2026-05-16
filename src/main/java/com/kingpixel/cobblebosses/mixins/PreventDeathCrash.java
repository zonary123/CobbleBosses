package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.model.Damageable;
import com.kingpixel.cobblebosses.util.BossUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PreventDeathCrash {
  @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
  private void cobbleBosses$heal(float amount, CallbackInfo ci) {
    LivingEntity livingEntity = (LivingEntity) (Object) this;
    if (!(livingEntity instanceof PokemonEntity entity)) return;
    Pokemon pokemon = entity.getPokemon();

    if (!pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) return;

    Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
    if (boss == null) return;

    Damageable damageable = boss.getDamageable();
    if (damageable == null || !damageable.isCatchable()) return;

    if (!pokemon.isUncatchable()) ci.cancel();
  }

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
