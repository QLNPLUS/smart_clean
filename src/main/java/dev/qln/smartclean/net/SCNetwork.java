package dev.qln.smartclean.net;

import dev.qln.smartclean.SmartClean;
import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.storage.TrashStore;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 分页回收站的网络层（Fabric 1.20.1 的旧版 channel 体系：Identifier + FriendlyByteBuf）。
 */
public final class SCNetwork {

    public static final ResourceLocation PAGE_REQUEST = new ResourceLocation(SmartClean.MODID, "page_request");
    public static final ResourceLocation TRASH_SYNC = new ResourceLocation(SmartClean.MODID, "trash_sync");

    private SCNetwork() {}

    public static void registerCommon() {
        ServerPlayNetworking.registerGlobalReceiver(PAGE_REQUEST, (server, player, handler, buf, responseSender) -> {
            int delta = buf.readVarInt();
            server.execute(() -> {
                if (player.containerMenu instanceof TrashMenu menu) {
                    menu.turnPage(delta);
                    menu.broadcastChanges();
                    sendSync(player, menu);
                }
            });
        });
    }

    public static void sendSync(ServerPlayer player, TrashMenu menu) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        long now = SmartClean.SERVICE.gameTime(server);
        TrashStore store = SmartClean.SERVICE.getStore();
        int[] remaining = store.remainingSeconds(menu.getPage(), now);

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(menu.getPage());
        buf.writeVarInt(store.pageCount());
        buf.writeVarInt(store.totalItems());
        for (int i = 0; i < TrashStore.SLOTS_PER_PAGE; i++) {
            int value = (remaining != null && i < remaining.length) ? remaining[i] : -1;
            buf.writeVarInt(value < 0 ? 0 : value + 1);
        }
        ServerPlayNetworking.send(player, TRASH_SYNC, buf);
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof TrashMenu menu) {
                sendSync(player, menu);
            }
        }
    }
}
