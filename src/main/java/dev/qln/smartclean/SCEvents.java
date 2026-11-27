package dev.qln.smartclean;

import dev.qln.smartclean.net.SCNetwork;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Fabric 事件接线：服务器启停、每 tick、实体载入/卸载。 */
public final class SCEvents {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SmartClean.SERVICE.setCurrentServer(server);
            SmartClean.SERVICE.applyConfig();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SmartClean.SERVICE.setCurrentServer(null));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SmartClean.SERVICE.onServerTick(server);
            SCNetwork.onServerTick(server);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> SmartClean.SERVICE.onEntityJoin(entity, world));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> SmartClean.SERVICE.onEntityLeave(entity, world));
    }

    private SCEvents() {}
}
