package dev.qln.smartclean.client;

import dev.qln.smartclean.menu.SCMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class SCClient {

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SCMenus.TRASH.get(), TrashScreen::new);
    }

    private SCClient() {}
}
