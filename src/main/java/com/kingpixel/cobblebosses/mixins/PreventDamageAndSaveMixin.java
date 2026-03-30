package com.kingpixel.cobblebosses.mixins;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixins combinados para bosses:
 * - Prevención de daño
 * - Prevención de guardado
 * - Prevención de riding/mount
 */
@Mixin(PokemonEntity.class)
public abstract class PreventDamageAndSaveMixin {

  @Unique
  private boolean isBossOrHighLevel(Entity entity) {
    if (entity instanceof PokemonEntity pokemonEntity) {
      Pokemon pokemon = pokemonEntity.getPokemon();
      return pokemon.getLevel() > CobbleBosses.oldLevelCap || pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID);
    }
    return false;
  }

  @Unique
  private void logPreventPassenger(Entity entity) {
    if (CobbleBosses.config.isDebug()) {
      CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, "Preventing passenger: " + entity.getSavedEntityId());
    }
  }

  @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
  private void cobbleBosses$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
    Pokemon pokemon = pokemonEntity.getPokemon();

    if (!pokemon.getPersistentData().contains(CobbleBosses.TAG_BOSS_ID)) return;

    cir.setReturnValue(false);
    cir.cancel();

    Entity attacker = source.getAttacker();
    if (!(attacker instanceof ServerPlayerEntity player)) return;

    Boss boss = CobbleBosses.bossesConfig.getBoss(pokemon);
    if (boss == null) return;

    Damageable damageable = boss.getDamageable();
    if (damageable != null && damageable.isEnabled()) {
      if (!damageable.isDownLife(pokemonEntity)) {
        if (damageable.isCatchable()) {
          PlayerUtils.sendMessage(player.getUuid(),
            CobbleBosses.language.getYouCanCatch(),
            CobbleBosses.config.getPrefix(),
            TypeMessage.CHAT);
          PokemonProperties.Companion.parse("uncatchable=no").apply(pokemon);
        } else {
          PlayerUtils.sendMessage(player.getUuid(),
            CobbleBosses.language.getYouCanFight(),
            CobbleBosses.config.getPrefix(),
            TypeMessage.CHAT);
        }
        pokemonEntity.setHealth(1.0F);
      }
    }

    if (!PermissionApi.hasPermission(player, CobbleBosses.MOD_ID + ".showrewards", 2)) return;
    AdvancedItemChance rewards = boss.getRewards();
    if (rewards != null) {
      rewards.openMenu(player, template -> {
      }, close -> UIManager.closeUI(close.getPlayer()));
    }
  }

  @Inject(method = "shouldSave", at = @At("HEAD"), cancellable = true)
  private void cobbleBosses$shouldSave(CallbackInfoReturnable<Boolean> cir) {
    PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
    if (isBossOrHighLevel(pokemonEntity)) {
      cir.setReturnValue(false);
      cir.cancel();
    }
  }

  @Unique
  private void preventRidingHelper(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    Entity entity = (Entity) (Object) this;
    if (isBossOrHighLevel(entity) || isBossOrHighLevel(passenger)) {
      logPreventPassenger(passenger);
      cir.setReturnValue(false);
      cir.cancel();
    }
  }

  @Inject(method = "canStartRiding", at = @At("HEAD"), cancellable = true)
  private void canStartRiding(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    preventRidingHelper(passenger, cir);
  }

  @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
  private void canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
    preventRidingHelper(passenger, cir);
  }
}