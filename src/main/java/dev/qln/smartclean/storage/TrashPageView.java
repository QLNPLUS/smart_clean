package dev.qln.smartclean.storage;

import dev.qln.smartclean.config.SCConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 把某一页的回收站存储包装成原版 {@link Container}，供菜单槽位读写。
 * 翻页时只切换页码，菜单本身不重建。
 */
public class TrashPageView implements Container {

    private final TrashStore store;
    private int page;

    public TrashPageView(TrashStore store, int page) {
        this.store = store;
        this.page = Math.max(0, page);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        int max = Math.max(0, store.pageCount() - 1);
        this.page = Math.max(0, Math.min(page, max));
        store.markChanged();
    }

    public TrashStore getStore() {
        return store;
    }

    @Override
    public int getContainerSize() {
        return TrashStore.SLOTS_PER_PAGE;
    }

    @Override
    public boolean isEmpty() {
        return store.isEmptyPage(page);
    }

    @Override
    public ItemStack getItem(int slot) {
        return store.getItem(page, slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return store.removeItem(page, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = store.getItem(page, slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        store.setItem(page, slot, ItemStack.EMPTY, 0L);
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        long expiry = store.getExpiry(page, slot);
        store.setItem(page, slot, stack, expiry);
    }

    @Override
    public void setChanged() {
        store.markChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return SCConfig.PLAYER_INSERT.get();
    }

    @Override
    public void clearContent() {
        store.clearPage(page);
    }
}
