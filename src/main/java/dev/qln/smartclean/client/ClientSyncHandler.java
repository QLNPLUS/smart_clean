package dev.qln.smartclean.client;

import dev.qln.smartclean.menu.TrashMenu;
import net.minecraft.client.Minecraft;

/** 客户端接收服务端同步的页码与各槽独立倒计时。 */
public final class ClientSyncHandler {

    public static void apply(int page, int pageCount, int totalItems, int[] remaining) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.player.containerMenu instanceof TrashMenu menu) {
            menu.acceptSync(page, pageCount, totalItems, remaining);
        }
    }

    private ClientSyncHandler() {}
}
