package dev.qln.smartclean.menu;

import dev.qln.smartclean.SmartClean;
import dev.qln.smartclean.storage.TrashPageView;
import dev.qln.smartclean.storage.TrashStore;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 分页回收站菜单：固定 54 个回收站槽位（每页），玩家背包与快捷栏照常显示。
 * 翻页只切换后端页码，不重建槽位。
 */
public class TrashMenu extends AbstractContainerMenu {

    public static final int TRASH_SLOTS = TrashStore.SLOTS_PER_PAGE;
    private static final int PLAYER_INV_START = TRASH_SLOTS;
    private static final int HOTBAR_START = PLAYER_INV_START + 27;

    private final TrashPageView view;
    private final Inventory playerInventory;

    private int page;
    private int pageCount = 1;
    private int totalItems;
    private int[] remaining = new int[TRASH_SLOTS];

    public TrashMenu(int windowId, Inventory playerInventory) {
        super(SCMenus.TRASH.get(), windowId);
        this.playerInventory = playerInventory;
        this.view = new TrashPageView(SmartClean.SERVICE.getStore(), 0);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new TrashSlot(this.view, col + row * 9, 30 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 30 + col * 18, 139 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 30 + col * 18, 197));
        }
    }

    public int getPage() {
        return page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getRemainingSeconds(int slot) {
        if (slot < 0 || slot >= remaining.length) return -1;
        return remaining[slot];
    }

    public int[] copyRemaining() {
        return remaining.clone();
    }

    /** 以回收站实际页数为准：服务端的 pageCount 字段不会被填充，必须查 store（否则翻页会被 clamp 回第 1 页）。 */
    public void setPage(int page) {
        int count = Math.max(1, SmartClean.SERVICE.getStore().pageCount());
        this.pageCount = count;
        int max = count - 1;
        this.page = Math.max(0, Math.min(page, max));
        this.view.setPage(this.page);
    }

    public void turnPage(int delta) {
        setPage(this.page + delta);
    }

    /** 服务端权威数据同步到菜单（客户端界面读取）。 */
    public void acceptSync(int page, int pageCount, int totalItems, int[] remaining) {
        this.page = page;
        this.pageCount = Math.max(1, pageCount);
        this.totalItems = totalItems;
        if (remaining != null && remaining.length == TRASH_SLOTS) {
            this.remaining = remaining.clone();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * 点击回收站槽位直接把物品收进玩家背包，不再走"先放到光标再拖"的流程。
     * 之前点击只把物品挪到光标上，这一格立刻显示为空，观感就像物品凭空丢了。
     * 背包放不下时把剩余部分退回原槽位（并给永不过期的保留时间），保证任何情况下都不会消失。
     */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!player.level().isClientSide()
                && clickType == ClickType.PICKUP
                && slotId >= 0 && slotId < TRASH_SLOTS) {
            Slot slot = this.slots.get(slotId);
            ItemStack current = slot.getItem();
            if (!current.isEmpty()) {
                int amount = button == 1 ? 1 : current.getCount();
                ItemStack taken = slot.remove(amount);
                if (!taken.isEmpty()) {
                    boolean added = player.getInventory().add(taken);
                    if (!added && !taken.isEmpty()) {
                        this.view.getStore().setItem(this.page, slotId, taken, TrashStore.NEVER);
                    }
                    slot.setChanged();
                    this.broadcastChanges();
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < this.slots.size() ? this.slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        if (index >= TRASH_SLOTS) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (!this.moveItemStackTo(stack, PLAYER_INV_START, this.slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
}
