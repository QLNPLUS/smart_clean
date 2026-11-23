package dev.qln.smartclean.client;

import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.net.SCNetwork;
import net.minecraft.client.Minecraft;

/** 客户端接收服务端同步的页码与各槽独立倒计时。 */
public final class ClientSyncHandler {

    public static void apply(SCNetwork.TrashSync msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.player.containerMenu instanceof TrashMenu menu) {
            menu.acceptSync(msg.page(), msg.pageCount(), msg.totalItems(), msg.remaining());
        }
    }

    private ClientSyncHandler() {}
}
