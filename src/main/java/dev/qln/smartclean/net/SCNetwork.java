package dev.qln.smartclean.net;

import dev.qln.smartclean.SmartClean;
import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.storage.TrashStore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * 分页回收站的网络层（NeoForge 1.21.1 payload 体系）。
 */
public final class SCNetwork {

    private SCNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PageRequest.TYPE, PageRequest.STREAM_CODEC, PageRequest::handle);
        registrar.playToClient(TrashSync.TYPE, TrashSync.STREAM_CODEC, TrashSync::handle);
    }

    public static void sendSync(ServerPlayer player, TrashMenu menu) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        long now = SmartClean.SERVICE.gameTime(server);
        TrashStore store = SmartClean.SERVICE.getStore();
        int[] remaining = store.remainingSeconds(menu.getPage(), now);
        PacketDistributor.sendToPlayer(player,
                new TrashSync(menu.getPage(), store.pageCount(), store.totalItems(), remaining));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof TrashMenu menu) {
                sendSync(player, menu);
            }
        }
    }

    public record PageRequest(int delta) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<PageRequest> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SmartClean.MODID, "page_request"));

        public static final StreamCodec<RegistryFriendlyByteBuf, PageRequest> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, PageRequest::delta, PageRequest::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(PageRequest payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player
                        && player.containerMenu instanceof TrashMenu menu) {
                    menu.turnPage(payload.delta());
                    menu.broadcastChanges();
                    sendSync(player, menu);
                }
            });
        }
    }

    public record TrashSync(int page, int pageCount, int totalItems, int[] remaining) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<TrashSync> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SmartClean.MODID, "trash_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, TrashSync> STREAM_CODEC =
                StreamCodec.of(TrashSync::encode, TrashSync::decode);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, TrashSync msg) {
            buf.writeVarInt(msg.page);
            buf.writeVarInt(msg.pageCount);
            buf.writeVarInt(msg.totalItems);
            for (int i = 0; i < TrashStore.SLOTS_PER_PAGE; i++) {
                int value = (msg.remaining != null && i < msg.remaining.length) ? msg.remaining[i] : -1;
                buf.writeVarInt(value < 0 ? 0 : value + 1);
            }
        }

        private static TrashSync decode(RegistryFriendlyByteBuf buf) {
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

        public static void handle(TrashSync payload, IPayloadContext context) {
            context.enqueueWork(() -> dev.qln.smartclean.client.ClientSyncHandler.apply(payload));
        }
    }
}
