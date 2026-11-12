package com.kingpixel.cobblebosses.mixins;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.util.BossUtil;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 14/02/2025 5:51
 */
@Mixin(PokemonEntity.class)
public abstract class PreventSaveMixin {

  @Unique
  private int cobbleBosses$savedRealLevel = -1;

  @Inject(method = "writeNbt", at = @At("HEAD"))
  private void cobbleBosses$beforeWriteNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
    PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
    if (BossUtil.isBoss(pokemonEntity)) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      if (pokemon != null) {
        cobbleBosses$savedRealLevel = pokemon.getLevel();
        pokemon.getPersistentData().putInt("boss_real_level", cobbleBosses$savedRealLevel);
        if (cobbleBosses$savedRealLevel > CobbleBosses.oldLevelCap) {
          pokemon.setLevel(CobbleBosses.oldLevelCap);
        }
      }
    }
  }

  @Inject(method = "writeNbt", at = @At("RETURN"))
  private void cobbleBosses$afterWriteNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
    if (cobbleBosses$savedRealLevel > CobbleBosses.oldLevelCap) {
      PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
      if (BossUtil.isBoss(pokemonEntity)) {
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon != null) {
          Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(CobbleBosses.maxLevelCap);
          pokemon.setLevel(cobbleBosses$savedRealLevel);
          Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(CobbleBosses.oldLevelCap);
        }
      }
    }
    cobbleBosses$savedRealLevel = -1;
  }
}
