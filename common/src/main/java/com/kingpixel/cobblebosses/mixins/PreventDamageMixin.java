package com.kingpixel.cobblebosses.mixins;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.api.PermissionApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 14/02/2025 4:40
 */
@Mixin(PokemonEntity.class)
public abstract class PreventDamageMixin {

  @Shadow public abstract boolean offerHeldItem(@NotNull PlayerEntity player, @NotNull ItemStack stack);

  @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
  private void PreventDamageMixin$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
    if (pokemonEntity == null) return;
    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon == null) return;
    if (pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) {
      Entity attacker = source.getAttacker();
      if (attacker == null) {
        cir.cancel();
        return;
      }
      if (attacker instanceof ServerPlayerEntity player) {
        Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
        if (boss == null) {
          if (CobbleBosses.config.isDebug()) {
            CobbleUtils.LOGGER.info("Boss not found for pokemon: " + pokemon);
          }
          return;
        }
        if (!PermissionApi.hasPermission(player, CobbleBosses.MOD_ID + ".showrewards", 2)) {
          cir.cancel();
          return;
        }
        AdvancedItemChance rewards = boss.getRewards();
        if (rewards == null) {
          if (CobbleBosses.config.isDebug()) {
            CobbleUtils.LOGGER.info("Rewards not found for boss: " + boss);
          }
          return;
        }
        rewards.openMenu(player, template -> {

        }, close -> UIManager.closeUI(close.getPlayer()));
      }
      cir.cancel();
    }
  }

  @Inject(method = "shouldSave", at = @At("HEAD"), cancellable = true)
  private void PreventDamageMixin$shouldSave(CallbackInfoReturnable<Boolean> cir) {
    PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
    if (pokemonEntity == null) return;
    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon == null) return;
    if (pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) {
      cir.setReturnValue(false);
    }
  }


}
