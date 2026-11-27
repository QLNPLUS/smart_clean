package dev.qln.smartclean;

import dev.qln.smartclean.command.SCCommands;
import dev.qln.smartclean.config.SCConfig;
import dev.qln.smartclean.menu.SCMenus;
import dev.qln.smartclean.net.SCNetwork;
import dev.qln.smartclean.service.TrashService;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartClean implements ModInitializer {

    public static final String MODID = "smartclean";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final TrashService SERVICE = new TrashService();

    @Override
    public void onInitialize() {
        SCConfig.load();

        SCMenus.register();
        SCNetwork.registerCommon();
        SCCommands.register();
        SCEvents.register();

        SERVICE.applyConfig();
        LOGGER.info("[SmartClean] initialized (Fabric 1.20.1).");
    }
}
