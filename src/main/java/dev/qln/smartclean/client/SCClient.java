package dev.qln.smartclean.client;

import dev.qln.smartclean.menu.SCMenus;
import dev.qln.smartclean.net.SCNetwork;
import dev.qln.smartclean.storage.TrashStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public final class SCClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(SCMenus.TRASH, TrashScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(SCNetwork.TRASH_SYNC, (client, handler, buf, responseSender) -> {
            int page = buf.readVarInt();
            int pageCount = buf.readVarInt();
            int totalItems = buf.readVarInt();
            int[] remaining = new int[TrashStore.SLOTS_PER_PAGE];
            for (int i = 0; i < remaining.length; i++) {
                int value = buf.readVarInt();
                remaining[i] = value <= 0 ? -1 : value - 1;
            }
            client.execute(() -> ClientSyncHandler.apply(page, pageCount, totalItems, remaining));
        });
    }
}
