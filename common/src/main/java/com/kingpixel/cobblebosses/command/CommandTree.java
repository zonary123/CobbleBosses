package com.kingpixel.cobblebosses.command;

import com.kingpixel.cobblebosses.CobbleBosses;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

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
                  return 1;
                }
              )
          )
      );
    }

  }


}
