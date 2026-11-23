package dev.qln.smartclean;

import com.mojang.logging.LogUtils;
import dev.qln.smartclean.command.SCCommands;
import dev.qln.smartclean.config.SCConfig;
import dev.qln.smartclean.menu.SCMenus;
import dev.qln.smartclean.net.SCNetwork;
import dev.qln.smartclean.service.TrashService;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SmartClean.MODID)
public class SmartClean {

    public static final String MODID = "smartclean";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TrashService SERVICE = new TrashService();

    public SmartClean(IEventBus modBus, ModContainer container) {
        SCConfig.register(container);
        modBus.addListener(SCConfig::onLoad);
        modBus.addListener(SCConfig::onReload);

        SCMenus.MENUS.register(modBus);
        modBus.addListener(SCNetwork::register);
        modBus.addListener(dev.qln.smartclean.client.SCClient::registerScreens);

        NeoForge.EVENT_BUS.register(SCCommands.class);
        NeoForge.EVENT_BUS.addListener(SERVICE::onServerTick);
        NeoForge.EVENT_BUS.addListener(SERVICE::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(SERVICE::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(SCNetwork::onServerTick);

        SERVICE.applyConfig();
        LOGGER.info("[SmartClean] initialized (NeoForge 1.21.1).");
    }
}
