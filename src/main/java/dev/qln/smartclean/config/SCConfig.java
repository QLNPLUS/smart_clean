package dev.qln.smartclean.config;

import dev.qln.smartclean.SmartClean;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public final class SCConfig {

    public static final ModConfigSpec SERVER_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_AUTO_COLLECT;
    public static final ModConfigSpec.IntValue TICKS_PER_COLLECT;
    public static final ModConfigSpec.IntValue TICKS_KEEP_AFTER_COLLECT;
    public static final ModConfigSpec.BooleanValue ANNOUNCE_MESSAGES;
    public static final ModConfigSpec.IntValue MAX_PAGES;
    public static final ModConfigSpec.BooleanValue PLAYER_INSERT;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SWEEP_LIFETIMES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> KEEP_DURATIONS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> COMPRESS_RULES;

    public static final ModConfigSpec.BooleanValue ENABLE_VOID_PROTECT;
    public static final ModConfigSpec.IntValue VOID_RAISE_MAX_STEPS;
    public static final ModConfigSpec.BooleanValue VOID_RESET_VELOCITY;

    public static final ModConfigSpec.ConfigValue<String> PREFIX;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> WARN_SECONDS;

    public static final ModConfigSpec.ConfigValue<String> MSG_PRE_COLLECT;
    public static final ModConfigSpec.ConfigValue<String> MSG_COLLECTED;
    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_SWEPT;
    public static final ModConfigSpec.ConfigValue<String> MSG_PURGED;
    public static final ModConfigSpec.ConfigValue<String> MSG_STATUS;
    public static final ModConfigSpec.ConfigValue<String> MSG_HELP;
    public static final ModConfigSpec.ConfigValue<String> MSG_CMD_COLLECT;
    public static final ModConfigSpec.ConfigValue<String> MSG_CMD_COLLECT_NONE;
    public static final ModConfigSpec.ConfigValue<String> MSG_CMD_PURGE;

    private static final java.util.function.Predicate<Object> ID_PREDICATE =
            o -> o instanceof String s && s.contains(":");

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        ENABLE_AUTO_COLLECT = b.comment("是否启用周期性自动回收世界掉落物。")
                .define("enableAutoCollect", true);
        TICKS_PER_COLLECT = b.comment("两次自动回收之间的间隔（tick）。20 tick = 1 秒。")
                .defineInRange("ticksPerCollect", 20 * 60 * 5, 20, 20 * 60 * 60);
        TICKS_KEEP_AFTER_COLLECT = b.comment("回收站内物品的默认保留时间（tick），到时自动清空。20 tick = 1 秒。0 表示永不自动清空。")
                .defineInRange("ticksKeepAfterCollect", 20 * 60 * 2, 0, 20 * 60 * 60);
        ANNOUNCE_MESSAGES = b.comment("是否向在线玩家广播提示消息（预警、已回收、已清空等）。")
                .define("announceMessages", true);
        b.pop();

        b.push("trash");
        MAX_PAGES = b.comment("回收站最多保留多少页（每页 54 格）。界面通过左右按钮翻页。")
                .defineInRange("maxPages", 8, 1, 64);
        PLAYER_INSERT = b.comment("是否允许玩家把物品主动放进回收站槽位。默认 false：回收站只能取出，槽位不接受放入。")
                .define("playerInsert", false);
        b.pop();

        b.push("blacklist");
        ITEM_BLACKLIST = b.comment("物品黑名单（物品 ID，形如 minecraft:nether_star）。黑名单物品不会被收集，也不会被清空。")
                .defineList("items", Arrays.asList("minecraft:nether_star", "minecraft:totem_of_undying"), ID_PREDICATE);
        b.pop();

        b.push("sweep_per_item");
        SWEEP_LIFETIMES = b.comment("""
                世界侧独立回收倒计时（优先级高于全局 ticksPerCollect）。
                格式：物品ID,秒数 —— 掉落物在世界上存在超过该秒数后会被单独回收进回收站。
                示例："minecraft:cobblestone,180" 表示圆石掉落 180 秒后被优先回收。
                未列出的物品继续跟随全局自动回收节奏。"""
        ).defineList("itemLifetimes", List.of(), ID_PREDICATE);
        b.pop();

        b.push("retention_per_item");
        KEEP_DURATIONS = b.comment("""
                回收站侧独立保留倒计时（优先级高于全局 ticksKeepAfterCollect）。
                格式：物品ID,秒数 —— 该物品进入回收站后按自己的秒数倒计时清空，0 表示永不清空。
                示例："minecraft:diamond,600" 表示钻石在回收站保留 600 秒。
                未列出的物品使用全局 ticksKeepAfterCollect。"""
        ).defineList("itemKeepDurations", List.of(), ID_PREDICATE);
        b.pop();

        b.push("compress");
        COMPRESS_RULES = b.comment("""
                回收站物品压缩规则。格式：源物品ID,源数量,目标物品ID,目标数量。
                当回收站内源物品数量达到源数量时，每满一份就替换为目标物品与目标数量（可反复触发）。
                示例："minecraft:oak_log,8,minecraft:oak_planks,6"：每 8 个原木换成 6 个木板。"""
        ).defineList("rules", List.of(), ID_PREDICATE);
        b.pop();

        b.push("void_protection");
        ENABLE_VOID_PROTECT = b.comment("是否启用虚空物品保护（掉落物掉入虚空时自动拉回到安全高度）。")
                .define("enableVoidProtect", true);
        VOID_RAISE_MAX_STEPS = b.comment("向上寻找空气可站立位置的最大步数。")
                .defineInRange("raiseMaxSteps", 6, 1, 32);
        VOID_RESET_VELOCITY = b.comment("拉回时是否将物品速度清零。")
                .define("resetVelocity", true);
        b.pop();

        b.push("messages");
        PREFIX = b.comment("消息前缀（支持渐变标签）。示例：<gradient:#00E5FF:#00FF7F>[清道夫]</gradient>")
                .define("prefix", "<gradient:#00E5FF:#00FF7F>[SmartClean]</gradient>");
        WARN_SECONDS = b.comment("自动回收前的预警秒数列表。")
                .defineList("warnSeconds", Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1),
                        o -> o instanceof Integer i && i >= 0);

        MSG_PRE_COLLECT = b.comment("预警消息模板。占位符：{time}（剩余秒数）。")
                .define("msgPreCollect", "即将在 {time}s 后回收世界掉落物，请及时拾取重要物品！");
        MSG_COLLECTED = b.comment("自动回收完成消息模板。占位符：{entities}、{keep}。")
                .define("msgCollected", "已回收掉落物，共 {entities} 个实体。使用 /smartclean gui 领取，按物品独立倒计时清空。");
        MSG_ITEM_SWEPT = b.comment("按物品独立倒计时回收后的消息模板。占位符：{entities}。留空则不广播。")
                .define("msgItemSwept", "已按独立倒计时回收 {entities} 个掉落物。");
        MSG_PURGED = b.comment("回收站槽位到期清空的消息模板。占位符：{slots}。")
                .define("msgPurged", "回收站有 {slots} 个槽位到期清空。");
        MSG_STATUS = b.comment("状态消息模板。占位符：{slotsUsed}、{slotsTotal}、{items}、{keep}、{auto}、{interval}、{keepCfg}、{announce}、{pages}。")
                .define("msgStatus", "回收站：{slotsUsed}/{slotsTotal} 槽（{pages} 页），物品 {items} 个；最近到期：{keep}；自动回收：{auto}，间隔：{interval}s，默认保留：{keepCfg}s，广播：{announce}。");
        MSG_HELP = b.comment("help 文本（支持渐变与换行）。")
                .define("msgHelp", "<gradient:#7AD7F0:#4FC3F7>SmartClean 帮助</gradient>\\n<gradient:#A8FF78:#78FFD6>/smartclean gui</gradient> - 打开分页回收站\\n<gradient:#A8FF78:#78FFD6>/smartclean status</gradient> - 查看状态\\n<gradient:#FFD54F:#FF8A00>/smartclean collect</gradient> - 立即回收（权限2）\\n<gradient:#FFD54F:#FF8A00>/smartclean purge</gradient> - 立即清空（权限2）\\n<gradient:#B388FF:#7C4DFF>/smartclean reload</gradient> - 重载配置（权限2）");
        MSG_CMD_COLLECT = b.comment("手动 collect 的反馈。占位符：{entities}、{keep}。")
                .define("msgCmdCollect", "已回收掉落物，共 {entities} 个实体。可在回收站按物品独立倒计时领取。");
        MSG_CMD_COLLECT_NONE = b.comment("没有可回收掉落物时的反馈。")
                .define("msgCmdCollectNone", "没有可清理的掉落物！");
        MSG_CMD_PURGE = b.comment("手动 purge 的反馈。占位符：{slots}。")
                .define("msgCmdPurge", "已清空回收站，清除了 {slots} 个槽位中的物品（黑名单物品已跳过）。");
        b.pop();

        SERVER_SPEC = b.build();
    }

    private SCConfig() {}

    public static void register(ModContainer container) {
        // 用 COMMON 而不是 SERVER：配置文件落在 config/ 目录（smartclean-common.toml），玩家可直接编辑，
        // 不再需要进存档的 serverconfig 目录才能改。
        container.registerConfig(ModConfig.Type.COMMON, SERVER_SPEC);
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            SmartClean.SERVICE.applyConfig();
        }
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            SmartClean.SERVICE.applyConfig();
        }
    }
}
