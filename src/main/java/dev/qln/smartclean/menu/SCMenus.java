package dev.qln.smartclean.menu;

import dev.qln.smartclean.SmartClean;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class SCMenus {

    public static final MenuType<TrashMenu> TRASH = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(SmartClean.MODID, "trash"),
            new MenuType<>(TrashMenu::new, FeatureFlags.VANILLA_SET));

    public static void register() {
        // 静态字段初始化时已完成注册，这里保留统一入口，方便以后加更多菜单。
    }

    private SCMenus() {}
}
