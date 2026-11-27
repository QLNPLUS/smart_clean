package dev.qln.smartclean.command;

import dev.qln.smartclean.SmartClean;
import dev.qln.smartclean.config.SCConfig;
import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.net.SCNetwork;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;

public final class SCCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("smartclean")
                        .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                        .then(Commands.literal("collect")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> collect(ctx.getSource())))
                        .then(Commands.literal("gui").executes(ctx -> openGui(ctx.getSource())))
                        .then(Commands.literal("purge")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> purge(ctx.getSource())))
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("reload")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> reload(ctx.getSource())))
        ));
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> SmartClean.SERVICE.helpMessage(), false);
        return 1;
    }

    private static int collect(CommandSourceStack src) {
        int moved = SmartClean.SERVICE.collectNow();
        if (moved > 0) {
            src.sendSuccess(() -> SmartClean.SERVICE.cmdCollectFeedback(moved), true);
        } else {
            src.sendSuccess(() -> SmartClean.SERVICE.cmdCollectNoneFeedback(), true);
        }
        return 1;
    }

    private static int openGui(CommandSourceStack src) {
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("只能由玩家执行。"));
            return 0;
        }
        MenuProvider provider = new SimpleMenuProvider(
                (windowId, playerInventory, p) -> new TrashMenu(windowId, playerInventory),
                Component.literal("回收站")
        );
        player.openMenu(provider);
        if (player.containerMenu instanceof TrashMenu menu) {
            SCNetwork.sendSync(player, menu);
        }
        return 1;
    }

    private static int purge(CommandSourceStack src) {
        int cleared = SmartClean.SERVICE.purgeNow();
        src.sendSuccess(() -> SmartClean.SERVICE.cmdPurgeFeedback(cleared), true);
        return 1;
    }

    private static int status(CommandSourceStack src) {
        src.sendSuccess(() -> SmartClean.SERVICE.statusMessage(), false);
        return 1;
    }

    private static int reload(CommandSourceStack src) {
        SCConfig.load();
        SmartClean.SERVICE.applyConfig();
        src.sendSuccess(() -> Component.literal("已从配置文件应用参数。"), true);
        return 1;
    }

    private SCCommands() {}
}
