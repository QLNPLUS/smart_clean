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
    /**
     * 是否允许真正改动底层存储。单人游戏里客户端与服务端共享同一个 TrashStore 实例，
     * 客户端的点击预测若直接改动它，服务端再处理同一个点击时就取不到物品，
     * 表现为"点一下就消失"。因此客户端视图只读，一律由服务端权威修改。
     */
    private final boolean canMutateStore;

    public TrashPageView(TrashStore store, int page) {
        this(store, page, true);
    }

    public TrashPageView(TrashStore store, int page, boolean canMutateStore) {
        this.store = store;
        this.page = Math.max(0, page);
        this.canMutateStore = canMutateStore;
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
        if (!canMutateStore) {
            // 客户端预测：只读，返回按数量切出的副本，绝不改共享存储
            ItemStack cur = store.getItem(page, slot);
            if (cur.isEmpty() || amount <= 0) return ItemStack.EMPTY;
            return cur.copyWithCount(Math.min(amount, cur.getCount()));
        }
        ItemStack out = store.removeItem(page, slot, amount);
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = store.getItem(page, slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        if (!canMutateStore) return current.copy();
        store.setItem(page, slot, ItemStack.EMPTY, 0L);
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!canMutateStore) return;
        long expiry = store.getExpiry(page, slot);
        store.setItem(page, slot, stack, expiry);
    }

    @Override
    public void setChanged() {
        if (!canMutateStore) return;
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
        if (!canMutateStore) return;
        store.clearPage(page);
    }
}
