package com.kingpixel.cobblebosses.command;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType;
import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobblebosses.config.OldConfig;
import com.kingpixel.cobblebosses.model.Boss;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * @author Carlos Varas Alonso - 10/06/2024 14:08
 */
public class CommandTree {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry) {
    for (String command : CobbleBosses.config.getCommands()) {
      LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal(command)
        .requires(source ->
          PermissionApi.hasPermission(source, CobbleBosses.MOD_ID + ".admin", 2));

      dispatcher.register(
        base
          .then(
            CommandManager.literal("reload")
              .executes(context -> {
                  CobbleBosses.load();
                  ServerPlayerEntity player = context.getSource().getPlayer();
                  if (player != null) {
                    PlayerUtils.sendMessage(
                      player,
                      CobbleBosses.language.getReload(),
                      CobbleBosses.config.getPrefix(),
                      TypeMessage.CHAT
                    );
                  } else {
                    CobbleUtils.LOGGER.info(CobbleBosses.MOD_ID, CobbleBosses.language.getReload()
                      .replace("%prefix%", CobbleBosses.config.getPrefix()));
                  }
                  return 1;
                }
              )
          ).then(
            CommandManager.literal("migrate")
              .executes(context -> {
                  OldConfig.migrate();
                  return 1;
                }
              )
          ).then(
            CommandManager.literal("spawn")
              .then(
                CommandManager.argument("boss", StringArgumentType.string())
                  .suggests((context, builder) -> {
                    for (Boss boss : CobbleBosses.bossesConfig.getBosses()) {
                      builder.suggest(boss.getId());
                    }
                    return builder.buildFuture();
                  })
                  // Player
                  .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                      .executes(context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
                        Vec3d pos = player.getPos();
                        spawnBoss(context, serverWorld, pos, PokemonProperties.Companion.parse("random"));
                        return 1;
                      }).then(
                        CommandManager.argument("pokemon", PokemonPropertiesArgumentType.Companion.properties())
                          .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
                            Vec3d pos = player.getPos();
                            PokemonProperties pokemonProperties = PokemonPropertiesArgumentType.Companion.getPokemonProperties(context, "pokemon");
                            spawnBoss(context, serverWorld, pos, pokemonProperties);
                            return 1;
                          })
                      )
                  )
                  // Coords
                  .then(
                    CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                      .then(
                        CommandManager.argument("world", StringArgumentType.string())
                          .suggests((context, builder) -> {
                            for (ServerWorld serverWorld : CobbleBosses.server.getWorlds()) {
                              builder.suggest(serverWorld.getRegistryKey().getValue().toString());
                            }
                            return builder.buildFuture();
                          })
                          .then(
                            CommandManager.argument("pokemon", PokemonPropertiesArgumentType.Companion.properties())
                              .executes(context -> {
                                ServerWorld serverWorld = getWorld(StringArgumentType.getString(context, "world"));
                                if (serverWorld == null) return 0;
                                Vec3d pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos").toCenterPos();
                                PokemonProperties pokemonProperties = PokemonPropertiesArgumentType.Companion.getPokemonProperties(context, "pokemon");
                                spawnBoss(context, serverWorld, pos, pokemonProperties);
                                return 1;
                              })
                          )
                      )
                  )
              )
          )
      );
    }

  }

  private static ServerWorld getWorld(String world) {
    for (ServerWorld serverWorld : CobbleBosses.server.getWorlds()) {
      if (serverWorld.getRegistryKey().getValue().toString().equals(world)) {
        return serverWorld;
      }
    }
    return null;
  }

  private static void spawnBoss(CommandContext<ServerCommandSource> context, ServerWorld serverWorld, Vec3d pos, PokemonProperties pokemonProperties) {
    String id = StringArgumentType.getString(context, "boss");
    var boss = CobbleBosses.bossesConfig.getBoss(id);
    if (boss == null) return;
    boss.spawn(serverWorld, pos, pokemonProperties.create());
  }

}
