package dev.qln.smartclean;

import com.mojang.logging.LogUtils;
import dev.qln.smartclean.command.SCCommands;
import dev.qln.smartclean.config.SCConfig;
import dev.qln.smartclean.menu.SCMenus;
import dev.qln.smartclean.net.SCNetwork;
import dev.qln.smartclean.service.TrashService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SmartClean.MODID)
public class SmartClean {

    public static final String MODID = "smartclean";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TrashService SERVICE = new TrashService();

    public SmartClean() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        SCConfig.register();
        modBus.addListener(SCConfig::onLoad);
        modBus.addListener(SCConfig::onReload);

        SCMenus.MENUS.register(modBus);
        SCNetwork.register();

        MinecraftForge.EVENT_BUS.register(SCCommands.class);
        MinecraftForge.EVENT_BUS.addListener(SERVICE::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(SERVICE::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(SERVICE::onEntityLeave);
        MinecraftForge.EVENT_BUS.addListener(SCNetwork::onServerTick);

        // 配置在 ModConfigEvent.Loading 里应用（SCConfig::onLoad），构造阶段读会抛
        // IllegalStateException: Cannot get config value before config is loaded。
        LOGGER.info("[SmartClean] Server + client mod initialized (Forge 1.19.2).");
    }
}
