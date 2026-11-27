package dev.qln.smartclean.client;

import dev.qln.smartclean.menu.SCMenus;
import dev.qln.smartclean.net.SCNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public final class SCClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(SCMenus.TRASH, TrashScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(SCNetwork.TrashSync.TYPE, (payload, context) ->
                context.client().execute(() -> ClientSyncHandler.apply(payload)));
    }
}
