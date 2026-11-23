package dev.qln.smartclean.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 回收站多页存储。
 *
 * <p>每个槽位独立记录一个到期时间（服务器 gameTime），实现「按物品 ID 的独立保留倒计时」：
 * 到期时间在放入时确定，不随全局配置变化，也不会因为其它物品到期而被清空。
 */
public class TrashStore {

    public static final int SLOTS_PER_PAGE = 54;
    /** 永不过期（配置为 0 秒或负数时使用）。 */
    public static final long NEVER = Long.MAX_VALUE;
    public static final int DEFAULT_MAX_PAGES = 8;

    private final List<ItemStack[]> stacks = new ArrayList<>();
    private final List<long[]> expires = new ArrayList<>();
    private int maxPages = DEFAULT_MAX_PAGES;
    private int revision;

    public void setMaxPages(int pages) {
        this.maxPages = Math.max(1, pages);
        while (stacks.size() > maxPages) {
            int last = stacks.size() - 1;
            stacks.remove(last);
            expires.remove(last);
        }
        this.revision++;
    }

    public int getMaxPages() {
        return maxPages;
    }

    private void ensurePage(int page) {
        while (stacks.size() <= page && stacks.size() < maxPages) {
            ItemStack[] arr = new ItemStack[SLOTS_PER_PAGE];
            long[] exp = new long[SLOTS_PER_PAGE];
            Arrays.fill(arr, ItemStack.EMPTY);
            stacks.add(arr);
            expires.add(exp);
        }
    }

    /** GUI 可显示的页数（至少 1 页）。 */
    public int pageCount() {
        return Math.max(1, stacks.size());
    }

    public ItemStack getItem(int page, int slot) {
        if (page < 0 || page >= stacks.size() || slot < 0 || slot >= SLOTS_PER_PAGE) return ItemStack.EMPTY;
        ItemStack stack = stacks.get(page)[slot];
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public long getExpiry(int page, int slot) {
        if (page < 0 || page >= stacks.size() || slot < 0 || slot >= SLOTS_PER_PAGE) return 0L;
        return expires.get(page)[slot];
    }

    public void setItem(int page, int slot, ItemStack stack, long expiry) {
        if (slot < 0 || slot >= SLOTS_PER_PAGE || page < 0 || page >= maxPages) return;
        ensurePage(page);
        if (page >= stacks.size()) return;
        stacks.get(page)[slot] = stack == null ? ItemStack.EMPTY : stack;
        expires.get(page)[slot] = expiry;
        revision++;
    }

    public ItemStack removeItem(int page, int slot, int amount) {
        ItemStack current = getItem(page, slot);
        if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack work = current.copy();
        ItemStack taken = work.split(amount);
        if (work.isEmpty()) {
            setItem(page, slot, ItemStack.EMPTY, 0L);
        } else {
            setItem(page, slot, work, getExpiry(page, slot));
        }
        return taken;
    }

    public boolean isEmptyPage(int page) {
        if (page < 0 || page >= stacks.size()) return true;
        for (ItemStack stack : stacks.get(page)) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    public int clearPage(int page) {
        if (page < 0 || page >= stacks.size()) return 0;
        ItemStack[] arr = stacks.get(page);
        long[] exp = expires.get(page);
        int cleared = 0;
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            if (!arr[i].isEmpty()) {
                arr[i] = ItemStack.EMPTY;
                exp[i] = 0L;
                cleared++;
            }
        }
        if (cleared > 0) revision++;
        return cleared;
    }

    private int firstEmptySlot(int page) {
        ItemStack[] arr = stacks.get(page);
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            if (arr[i].isEmpty()) return i;
        }
        return -1;
    }

    private int firstPageWithEmptySlot() {
        for (int p = 0; p < stacks.size(); p++) {
            if (firstEmptySlot(p) >= 0) return p;
        }
        if (stacks.size() < maxPages) {
            ensurePage(stacks.size());
            return stacks.size() - 1;
        }
        return -1;
    }

    /**
     * 放入物品。keepTicks 为该物品本次的保留时长（tick），<= 0 表示永不过期。
     *
     * @return 未能放入的数量（0 表示全部放入）
     */
    public int insert(ItemStack stack, long keepTicks, long now) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemStack remainder = stack.copy();

        for (int p = 0; p < stacks.size() && !remainder.isEmpty(); p++) {
            ItemStack[] arr = stacks.get(p);
            for (int i = 0; i < SLOTS_PER_PAGE && !remainder.isEmpty(); i++) {
                ItemStack current = arr[i];
                if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, remainder)) continue;
                int canMove = Math.min(current.getMaxStackSize() - current.getCount(), remainder.getCount());
                if (canMove > 0) {
                    current.grow(canMove);
                    remainder.shrink(canMove);
                    revision++;
                }
            }
        }

        while (!remainder.isEmpty()) {
            int page = firstPageWithEmptySlot();
            if (page < 0) break;
            int slot = firstEmptySlot(page);
            if (slot < 0) break;
            int move = Math.min(remainder.getCount(), remainder.getMaxStackSize());
            ItemStack put = remainder.copy();
            put.setCount(move);
            long expiry = keepTicks <= 0 ? NEVER : now + keepTicks;
            setItem(page, slot, put, expiry);
            remainder.shrink(move);
        }
        return remainder.getCount();
    }

    public int countItem(Item item) {
        int total = 0;
        for (ItemStack[] arr : stacks) {
            for (ItemStack stack : arr) {
                if (!stack.isEmpty() && stack.getItem() == item) total += stack.getCount();
            }
        }
        return total;
    }

    /** 按 id 移除指定数量，不区分 NBT。返回实际移除数量。 */
    public int removeItemAmount(Item item, int amount) {
        int removed = 0;
        for (int p = 0; p < stacks.size() && removed < amount; p++) {
            ItemStack[] arr = stacks.get(p);
            for (int i = 0; i < SLOTS_PER_PAGE && removed < amount; i++) {
                ItemStack current = arr[i];
                if (current.isEmpty() || current.getItem() != item) continue;
                int take = Math.min(current.getCount(), amount - removed);
                ItemStack rest = current.copy();
                rest.shrink(take);
                if (rest.isEmpty()) {
                    setItem(p, i, ItemStack.EMPTY, 0L);
                } else {
                    setItem(p, i, rest, getExpiry(p, i));
                }
                removed += take;
            }
        }
        return removed;
    }

    /** 清理解除到期的槽位，返回被清空的槽数。 */
    public int sweepExpired(long now) {
        int cleared = 0;
        for (int p = 0; p < stacks.size(); p++) {
            ItemStack[] arr = stacks.get(p);
            long[] exp = expires.get(p);
            for (int i = 0; i < SLOTS_PER_PAGE; i++) {
                if (arr[i].isEmpty()) {
                    exp[i] = 0L;
                    continue;
                }
                long at = exp[i];
                if (at != 0L && at != NEVER && now >= at) {
                    arr[i] = ItemStack.EMPTY;
                    exp[i] = 0L;
                    cleared++;
                }
            }
        }
        if (cleared > 0) revision++;
        return cleared;
    }

    /** 清空回收站，skip 命中的物品保留。返回被清空的槽数。 */
    public int purge(Predicate<ItemStack> skip) {
        int cleared = 0;
        for (int p = 0; p < stacks.size(); p++) {
            ItemStack[] arr = stacks.get(p);
            long[] exp = expires.get(p);
            for (int i = 0; i < SLOTS_PER_PAGE; i++) {
                ItemStack current = arr[i];
                if (current.isEmpty()) continue;
                if (skip != null && skip.test(current)) continue;
                arr[i] = ItemStack.EMPTY;
                exp[i] = 0L;
                cleared++;
            }
        }
        if (cleared > 0) revision++;
        return cleared;
    }

    public int usedSlots() {
        int used = 0;
        for (ItemStack[] arr : stacks) {
            for (ItemStack stack : arr) {
                if (!stack.isEmpty()) used++;
            }
        }
        return used;
    }

    public int totalItems() {
        int total = 0;
        for (ItemStack[] arr : stacks) {
            for (ItemStack stack : arr) {
                if (!stack.isEmpty()) total += stack.getCount();
            }
        }
        return total;
    }

    /** 当前最早到期槽位的剩余 tick；无安排返回 -1。 */
    public long earliestRemaining(long now) {
        long best = -1L;
        for (int p = 0; p < stacks.size(); p++) {
            ItemStack[] arr = stacks.get(p);
            long[] exp = expires.get(p);
            for (int i = 0; i < SLOTS_PER_PAGE; i++) {
                if (arr[i].isEmpty()) continue;
                long at = exp[i];
                if (at == 0L || at == NEVER) continue;
                long remain = at - now;
                if (remain < 0) remain = 0;
                if (best < 0 || remain < best) best = remain;
            }
        }
        return best;
    }

    /** 某页各槽位的剩余秒数（-1 表示不自动清空）。 */
    public int[] remainingSeconds(int page, long now) {
        int[] out = new int[SLOTS_PER_PAGE];
        Arrays.fill(out, -1);
        if (page < 0 || page >= stacks.size()) return out;
        ItemStack[] arr = stacks.get(page);
        long[] exp = expires.get(page);
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            if (arr[i].isEmpty()) continue;
            long at = exp[i];
            if (at == 0L || at == NEVER) continue;
            long remain = at - now;
            if (remain < 0) remain = 0;
            out[i] = (int) ((remain + 19L) / 20L);
        }
        return out;
    }

    public int revision() {
        return revision;
    }

    public void markChanged() {
        revision++;
    }
}
