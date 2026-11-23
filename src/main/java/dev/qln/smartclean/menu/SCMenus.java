package dev.qln.smartclean.menu;

import dev.qln.smartclean.SmartClean;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SCMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SmartClean.MODID);

    public static final RegistryObject<MenuType<TrashMenu>> TRASH =
            MENUS.register("trash", () -> IForgeMenuType.create(
                    (windowId, playerInventory, data) -> new TrashMenu(windowId, playerInventory)));

    private SCMenus() {}
}
