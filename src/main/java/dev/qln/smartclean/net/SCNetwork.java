package dev.qln.smartclean.net;

import dev.qln.smartclean.SmartClean;
import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.storage.TrashStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 分页回收站的网络层：
 * <ul>
 *   <li>C2S {@link PageRequest}：客户端点击左右翻页按钮</li>
 *   <li>S2C {@link TrashSync}：页码、页数、总物品数与各槽独立倒计时剩余秒数</li>
 * </ul>
 */
public final class SCNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SmartClean.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private SCNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, PageRequest.class, PageRequest::encode, PageRequest::decode,
                PageRequest::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, TrashSync.class, TrashSync::encode, TrashSync::decode,
                TrashSync::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendSync(ServerPlayer player, TrashMenu menu) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        long now = SmartClean.SERVICE.gameTime(server);
        TrashStore store = SmartClean.SERVICE.getStore();
        int[] remaining = store.remainingSeconds(menu.getPage(), now);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new TrashSync(menu.getPage(), store.pageCount(), store.totalItems(), remaining));
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof TrashMenu menu) {
                sendSync(player, menu);
            }
        }
    }

    /** 客户端 -> 服务端：翻页请求。delta 为 +1/-1（也接受任意页偏移）。 */
    public record PageRequest(int delta) {

        public static void encode(PageRequest msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.delta);
        }

        public static PageRequest decode(FriendlyByteBuf buf) {
            return new PageRequest(buf.readVarInt());
        }

        public static void handle(PageRequest msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player == null) return;
                if (!(player.containerMenu instanceof TrashMenu menu)) return;
                menu.turnPage(msg.delta);
                menu.broadcastChanges();
                sendSync(player, menu);
            });
            ctx.setPacketHandled(true);
        }
    }

    /** 服务端 -> 客户端：当前页同步数据。 */
    public record TrashSync(int page, int pageCount, int totalItems, int[] remaining) {

        public static void encode(TrashSync msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.page);
            buf.writeVarInt(msg.pageCount);
            buf.writeVarInt(msg.totalItems);
            int len = msg.remaining == null ? 0 : msg.remaining.length;
            for (int i = 0; i < TrashStore.SLOTS_PER_PAGE; i++) {
                int value = i < len ? msg.remaining[i] : -1;
                buf.writeVarInt(value < 0 ? 0 : value + 1);
            }
        }

        public static TrashSync decode(FriendlyByteBuf buf) {
            int page = buf.readVarInt();
            int pageCount = buf.readVarInt();
            int totalItems = buf.readVarInt();
            int[] remaining = new int[TrashStore.SLOTS_PER_PAGE];
            for (int i = 0; i < TrashStore.SLOTS_PER_PAGE; i++) {
                int value = buf.readVarInt();
                remaining[i] = value <= 0 ? -1 : value - 1;
            }
            return new TrashSync(page, pageCount, totalItems, remaining);
        }

        public static void handle(TrashSync msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> dev.qln.smartclean.client.ClientSyncHandler.apply(msg)));
            ctx.setPacketHandled(true);
        }
    }
}
