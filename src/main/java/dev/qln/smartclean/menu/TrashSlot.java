package dev.qln.smartclean.menu;

import dev.qln.smartclean.config.SCConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 回收站槽位：默认只取出，不允许放入；config 里的 trash.playerInsert 打开后才允许玩家主动放入。 */
public class TrashSlot extends Slot {

    public TrashSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return SCConfig.PLAYER_INSERT.get();
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}
