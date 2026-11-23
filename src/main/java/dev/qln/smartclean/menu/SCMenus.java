package dev.qln.smartclean.menu;

import dev.qln.smartclean.SmartClean;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class SCMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SmartClean.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<TrashMenu>> TRASH =
            MENUS.register("trash", () -> IMenuTypeExtension.create(
                    (windowId, playerInventory, data) -> new TrashMenu(windowId, playerInventory)));

    private SCMenus() {}
}
