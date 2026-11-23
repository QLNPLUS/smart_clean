package dev.qln.smartclean.service;

import dev.qln.smartclean.config.SCConfig;
import dev.qln.smartclean.storage.CompressRule;
import dev.qln.smartclean.storage.TrashStore;
import dev.qln.smartclean.util.MessageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.Predicate;

/**
 * 回收站核心服务：全局定时回收、按物品 ID 的独立世界侧倒计时、回收站独立保留倒计时、
 * 物品压缩、黑名单、虚空保护与全部消息广播。
 */
public class TrashService {

    private int intervalTicks = 20 * 60 * 5;
    private int keepTicks = 20 * 60 * 2;
    private boolean autoCollect = true;
    private boolean announce = true;
    private int maxPages = TrashStore.DEFAULT_MAX_PAGES;

    private String prefixRaw = "[SmartClean]";
    private List<Integer> warnSeconds = Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1);

    private String msgPreCollect = "即将在 {time}s 后回收世界掉落物，请及时拾取重要物品！";
    private String msgCollected = "已回收掉落物，共 {entities} 个实体。使用 /smartclean gui 领取。";
    private String msgItemSwept = "已按独立倒计时回收 {entities} 个掉落物。";
    private String msgPurged = "回收站有 {slots} 个槽位到期清空。";
    private String msgStatus = "回收站：{slotsUsed}/{slotsTotal} 槽（{pages} 页）。";
    private String msgHelp = "SmartClean 帮助";
    private String msgCmdCollect = "已回收掉落物，共 {entities} 个实体。";
    private String msgCmdCollectNone = "没有可清理的掉落物！";
    private String msgCmdPurge = "已清空回收站，清除了 {slots} 个槽位中的物品（黑名单物品已跳过）。";

    private boolean voidProtect = true;
    private int voidRaiseMax = 6;
    private boolean voidResetVel = true;

    private static final int VOID_PROTECT_BUDGET_PER_TICK = 1024;
    private static final int PER_ITEM_SWEEP_BUDGET_PER_TICK = 2048;

    private final Set<ResourceLocation> blacklist = new HashSet<>();
    private final Map<ResourceLocation, Integer> sweepLifetimes = new HashMap<>();
    private final Map<ResourceLocation, Integer> keepDurations = new HashMap<>();
    private final List<CompressRule> compressRules = new ArrayList<>();

    private final TrashStore store = new TrashStore();

    private int collectCountdown;
    private final Set<Integer> warnedThisCycle = new HashSet<>();
    /** 所有被跟踪的掉落物；value 为独立到期 gameTime（0 表示跟随全局节奏）。 */
    private final Map<ItemEntity, Long> tracked = new HashMap<>();
    private int tickCounter;

    public TrashStore getStore() {
        return store;
    }

    public synchronized void applyConfig() {
        this.intervalTicks = SCConfig.TICKS_PER_COLLECT.get();
        this.keepTicks = SCConfig.TICKS_KEEP_AFTER_COLLECT.get();
        this.autoCollect = SCConfig.ENABLE_AUTO_COLLECT.get();
        this.announce = SCConfig.ANNOUNCE_MESSAGES.get();
        this.maxPages = SCConfig.MAX_PAGES.get();
        this.prefixRaw = SCConfig.PREFIX.get();

        List<? extends Integer> ws = SCConfig.WARN_SECONDS.get();
        this.warnSeconds = new ArrayList<>();
        for (Object o : ws) if (o instanceof Integer i && i >= 0) this.warnSeconds.add(i);
        this.warnSeconds.sort(Comparator.reverseOrder());

        this.msgPreCollect = SCConfig.MSG_PRE_COLLECT.get();
        this.msgCollected = SCConfig.MSG_COLLECTED.get();
        this.msgItemSwept = SCConfig.MSG_ITEM_SWEPT.get();
        this.msgPurged = SCConfig.MSG_PURGED.get();
        this.msgStatus = SCConfig.MSG_STATUS.get();
        this.msgHelp = SCConfig.MSG_HELP.get().replace("\\n", "\n");
        this.msgCmdCollect = SCConfig.MSG_CMD_COLLECT.get();
        this.msgCmdCollectNone = SCConfig.MSG_CMD_COLLECT_NONE.get();
        this.msgCmdPurge = SCConfig.MSG_CMD_PURGE.get();

        this.voidProtect = SCConfig.ENABLE_VOID_PROTECT.get();
        this.voidRaiseMax = SCConfig.VOID_RAISE_MAX_STEPS.get();
        this.voidResetVel = SCConfig.VOID_RESET_VELOCITY.get();

        this.blacklist.clear();
        for (String s : SCConfig.ITEM_BLACKLIST.get()) {
            ResourceLocation id = CompressRule.parseId(s);
            if (id != null) this.blacklist.add(id);
        }

        this.sweepLifetimes.clear();
        parseIdSeconds(SCConfig.SWEEP_LIFETIMES.get(), this.sweepLifetimes);
        this.keepDurations.clear();
        parseIdSeconds(SCConfig.KEEP_DURATIONS.get(), this.keepDurations);

        this.compressRules.clear();
        for (String s : SCConfig.COMPRESS_RULES.get()) {
            CompressRule rule = CompressRule.parse(s);
            if (rule != null) this.compressRules.add(rule);
        }

        store.setMaxPages(maxPages);

        int oldInterval = this.intervalTicks;
        int effective = Math.max(20, intervalTicks);
        if (collectCountdown <= 0 || collectCountdown > effective || oldInterval != effective) {
            collectCountdown = effective;
            warnedThisCycle.clear();
        }
    }

    private static void parseIdSeconds(List<? extends String> lines, Map<ResourceLocation, Integer> out) {
        for (String raw : lines) {
            if (raw == null) continue;
            String[] parts = raw.split(",");
            if (parts.length != 2) continue;
            ResourceLocation id = CompressRule.parseId(parts[0]);
            if (id == null) continue;
            try {
                out.put(id, Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public long gameTime(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    // ------------------------------------------------------------------
    // 事件
    // ------------------------------------------------------------------

    public void onServerTick(ServerTickEvent.Post event) {

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        long now = gameTime(server);
        tickCounter++;

        if (autoCollect) {
            int secBefore = (collectCountdown + 19) / 20;
            if (announce && warnSeconds.contains(secBefore) && warnedThisCycle.add(secBefore)) {
                broadcast(server, msgPreCollect, Map.of("time", String.valueOf(secBefore)));
            }
            if (--collectCountdown <= 0) {
                int moved = collectNow(server, now);
                if (moved > 0 && announce) {
                    broadcast(server, msgCollected, Map.of(
                            "entities", String.valueOf(moved),
                            "keep", String.valueOf(keepTicks / 20)
                    ));
                }
                collectCountdown = Math.max(20, intervalTicks);
                warnedThisCycle.clear();
            }
        }

        sweepWorldItemsByOwnCountdown(server, now);

        if (tickCounter % 20 == 0) {
            int cleared = store.sweepExpired(now);
            if (cleared > 0) {
                applyCompression(now);
                if (announce) {
                    broadcast(server, msgPurged, Map.of("slots", String.valueOf(cleared)));
                }
            }
        }

        if (voidProtect && !tracked.isEmpty()) {
            protectVoidItems();
        }
    }

    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity ie)) return;
        Long expiry = Long.valueOf(0L);
        Integer seconds = sweepLifetimes.get(idOf(ie.getItem()));
        if (seconds != null && seconds > 0) {
            expiry = gameTimeOf(ie) + (long) seconds * 20L;
        }
        tracked.put(ie, expiry);
    }

    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof ItemEntity ie) {
            tracked.remove(ie);
        }
    }

    private long gameTimeOf(ItemEntity ie) {
        if (ie.level() instanceof ServerLevel level) return level.getGameTime();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? 0L : gameTime(server);
    }

    /** 世界侧独立倒计时：到点的掉落物立刻回收，优先级高于全局 collect 间隔。 */
    private void sweepWorldItemsByOwnCountdown(MinecraftServer server, long now) {
        if (tracked.isEmpty() || sweepLifetimes.isEmpty()) return;
        Iterator<Map.Entry<ItemEntity, Long>> it = tracked.entrySet().iterator();
        int processed = 0;
        int swept = 0;
        while (it.hasNext() && processed < PER_ITEM_SWEEP_BUDGET_PER_TICK) {
            Map.Entry<ItemEntity, Long> entry = it.next();
            processed++;
            ItemEntity ie = entry.getKey();
            long expiry = entry.getValue();
            if (ie.isRemoved() || ie.level().isClientSide) {
                it.remove();
                continue;
            }
            if (expiry <= 0L || now < expiry) continue;
            it.remove();
            ItemStack stack = ie.getItem();
            if (stack.isEmpty() || isBlacklisted(stack)) continue;
            int leftover = store.insert(stack, keepTicksFor(stack), now);
            if (leftover <= 0) {
                ie.discard();
                swept++;
            } else if (leftover != stack.getCount()) {
                ie.setItem(stack.copyWithCount(leftover));
                swept++;
            }
        }
        if (swept > 0) {
            applyCompression(now);
            if (announce && msgItemSwept != null && !msgItemSwept.isBlank()) {
                broadcast(server, msgItemSwept, Map.of("entities", String.valueOf(swept)));
            }
        }
    }

    private void protectVoidItems() {
        int processed = 0;
        Iterator<Map.Entry<ItemEntity, Long>> it = tracked.entrySet().iterator();
        while (it.hasNext() && processed < VOID_PROTECT_BUDGET_PER_TICK) {
            Map.Entry<ItemEntity, Long> entry = it.next();
            ItemEntity ie = entry.getKey();
            if (ie.isRemoved() || ie.level().isClientSide) {
                it.remove();
                continue;
            }
            if (!(ie.level() instanceof ServerLevel level)) {
                it.remove();
                continue;
            }
            int minY = level.getMinBuildHeight();
            if (ie.getY() < minY) {
                double x = Math.floor(ie.getX()) + 0.5;
                double z = Math.floor(ie.getZ()) + 0.5;
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int) Math.floor(x), minY + 1, (int) Math.floor(z));
                int steps = 0;
                while (steps < voidRaiseMax && !level.isEmptyBlock(pos)) {
                    pos.setY(pos.getY() + 1);
                    steps++;
                }
                if (level.isEmptyBlock(pos.below())) {
                    pos.setY(pos.getY() + 1);
                }
                ie.setPos(x, pos.getY() + 0.25, z);
                if (voidResetVel) ie.setDeltaMovement(0, 0, 0);
                ie.setOnGround(true);
            }
            processed++;
        }
    }

    // ------------------------------------------------------------------
    // 回收 / 清空 / 压缩
    // ------------------------------------------------------------------

    public int collectNow() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        return collectNow(server, gameTime(server));
    }

    public int collectNow(MinecraftServer server, long now) {
        int moved = 0;
        for (ServerLevel level : server.getAllLevels()) {
            AABB box = new AABB(-3.0E7, level.getMinBuildHeight(), -3.0E7,
                    3.0E7, level.getMaxBuildHeight(), 3.0E7);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box,
                    e -> !e.isRemoved() && !e.getItem().isEmpty());
            for (ItemEntity entity : items) {
                ItemStack stack = entity.getItem();
                if (isBlacklisted(stack)) continue;
                int leftover = store.insert(stack, keepTicksFor(stack), now);
                if (leftover <= 0) {
                    entity.discard();
                    tracked.remove(entity);
                    moved++;
                } else if (leftover != stack.getCount()) {
                    entity.setItem(stack.copyWithCount(leftover));
                    moved++;
                }
            }
        }
        if (moved > 0) {
            applyCompression(now);
        }
        return moved;
    }

    public int purgeNow() {
        int cleared = store.purge(this::isBlacklisted);
        return cleared;
    }

    /**
     * 压缩规则：回收站内某物品达到源数量时，替换为目标物品与数量，可反复触发。
     */
    public void applyCompression(long now) {
        if (compressRules.isEmpty()) return;
        boolean changed = true;
        int guard = 0;
        while (changed && guard++ < 512) {
            changed = false;
            for (CompressRule rule : compressRules) {
                Item fromItem = BuiltInRegistries.ITEM.get(rule.from());
                Item toItem = BuiltInRegistries.ITEM.get(rule.to());
                if (fromItem == null || toItem == null) continue;
                int total = store.countItem(fromItem);
                if (total < rule.fromCount()) continue;
                int rounds = total / rule.fromCount();
                int removed = store.removeItemAmount(fromItem, rounds * rule.fromCount());
                if (removed < rule.fromCount()) continue;
                int effectiveRounds = removed / rule.fromCount();
                ItemStack out = new ItemStack(toItem, effectiveRounds * rule.toCount());
                store.insert(out, keepTicksForId(rule.to()), now);
                changed = true;
            }
        }
    }

    public long keepTicksFor(ItemStack stack) {
        ResourceLocation id = idOf(stack);
        if (id != null) {
            Integer seconds = keepDurations.get(id);
            if (seconds != null) return seconds <= 0 ? 0L : (long) seconds * 20L;
        }
        return keepTicks <= 0 ? 0L : (long) keepTicks;
    }

    private long keepTicksForId(ResourceLocation id) {
        Integer seconds = keepDurations.get(id);
        if (seconds != null) return seconds <= 0 ? 0L : (long) seconds * 20L;
        return keepTicks <= 0 ? 0L : (long) keepTicks;
    }

    public boolean isBlacklisted(ItemStack stack) {
        ResourceLocation id = idOf(stack);
        return id != null && blacklist.contains(id);
    }

    private static ResourceLocation idOf(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    // ------------------------------------------------------------------
    // 消息
    // ------------------------------------------------------------------

    public Component statusMessage() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        long now = server == null ? 0L : gameTime(server);
        int slotsTotal = store.pageCount() * TrashStore.SLOTS_PER_PAGE;
        int slotsUsed = store.usedSlots();
        int items = store.totalItems();
        long remain = store.earliestRemaining(now);
        String keepDisp = remain < 0 ? "无安排" : ((remain + 19) / 20) + "s";

        Map<String, String> ph = new HashMap<>();
        ph.put("slotsUsed", String.valueOf(slotsUsed));
        ph.put("slotsTotal", String.valueOf(slotsTotal));
        ph.put("items", String.valueOf(items));
        ph.put("keep", keepDisp);
        ph.put("auto", String.valueOf(autoCollect));
        ph.put("interval", String.valueOf(intervalTicks / 20));
        ph.put("keepCfg", String.valueOf(keepTicks / 20));
        ph.put("announce", String.valueOf(announce));
        ph.put("pages", String.valueOf(store.pageCount()));
        return MessageUtil.withPrefix(MessageUtil.render(prefixRaw), MessageUtil.render(msgStatus, ph));
    }

    public Component helpMessage() {
        return MessageUtil.withPrefix(MessageUtil.render(prefixRaw), MessageUtil.render(msgHelp));
    }

    public Component cmdCollectFeedback(int moved) {
        Map<String, String> ph = Map.of(
                "entities", String.valueOf(moved),
                "keep", String.valueOf(keepTicks / 20)
        );
        return MessageUtil.withPrefix(MessageUtil.render(prefixRaw), MessageUtil.render(msgCmdCollect, ph));
    }

    public Component cmdCollectNoneFeedback() {
        return MessageUtil.withPrefix(MessageUtil.render(prefixRaw), MessageUtil.render(msgCmdCollectNone));
    }

    public Component cmdPurgeFeedback(int cleared) {
        return MessageUtil.withPrefix(MessageUtil.render(prefixRaw),
                MessageUtil.render(msgCmdPurge, Map.of("slots", String.valueOf(cleared))));
    }

    private void broadcast(MinecraftServer server, String template, Map<String, String> ph) {
        if (template == null || template.isBlank()) return;
        Component prefix = MessageUtil.render(prefixRaw);
        Component content = MessageUtil.render(template, ph);
        Component message = MessageUtil.withPrefix(prefix, content);
        server.getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(message));
    }

    /** 供外部（命令/网络）判断某掉落物是否受独立倒计时管理。 */
    public Predicate<ItemStack> blacklistPredicate() {
        return this::isBlacklisted;
    }
}
