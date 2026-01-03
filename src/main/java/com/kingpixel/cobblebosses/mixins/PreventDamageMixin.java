package com.kingpixel.cobblebosses.mixins;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobblebosses.model.Damageable;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 14/02/2025 4:40
 */
@Mixin(PokemonEntity.class)
public abstract class PreventDamageMixin {

  @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
  private void PreventDamageMixin$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
    if (pokemonEntity == null) return;
    Pokemon pokemon = pokemonEntity.getPokemon();
    if (pokemon == null) return;
    if (pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) {
      Entity attacker = source.getAttacker();
      if (attacker == null) return;
      if (attacker instanceof ServerPlayerEntity player) {
        Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
        if (boss == null) {
          if (CobbleBosses.config.isDebug()) {
            CobbleUtils.LOGGER.info("Boss not found for pokemon: " + pokemon);
          }
          return;
        }
        if (boss.getDamageable() != null && boss.getDamageable().isEnabled()) {
            Damageable damageable = boss.getDamageable();
            float percentageOfLife = (100 * pokemonEntity.getHealth() / pokemon.getMaxHealth());
            if (percentageOfLife > damageable.getUntilLifePercentage()) { // Is damageable yet
                return;
            } else {
                PlayerUtils.sendMessage(player.getUuid(),
                        CobbleBosses.language.getYouCanCatch(),
                        CobbleBosses.config.getPrefix(),
                        TypeMessage.CHAT);
            }
        }
        if (!PermissionApi.hasPermission(player, CobbleBosses.MOD_ID + ".showrewards", 2)) return;
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
      cir.setReturnValue(false);
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
