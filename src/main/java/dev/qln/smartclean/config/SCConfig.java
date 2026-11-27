package dev.qln.smartclean.config;

import dev.qln.smartclean.SmartClean;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabric 版配置：Fabric 没有 ForgeConfigSpec，这里用一个极简 TOML 读写器，
 * 保持与其他加载器一致的配置文件路径（config/smartclean-common.toml）与键名。
 *
 * <p>支持的形式只有：`[section]` 分段 + `key = value`，value 可以是
 * true/false、整数、带引号的字符串、方括号数组（字符串或整数）。
 */
public final class SCConfig {

    public static boolean ENABLE_AUTO_COLLECT = true;
    public static int TICKS_PER_COLLECT = 20 * 60 * 5;
    public static int TICKS_KEEP_AFTER_COLLECT = 20 * 60 * 2;
    public static boolean ANNOUNCE_MESSAGES = true;

    public static int MAX_PAGES = 8;
    public static boolean PLAYER_INSERT = false;
    public static boolean SHOW_SLOT_TIMERS = false;

    public static List<String> ITEM_BLACKLIST = Arrays.asList("minecraft:nether_star", "minecraft:totem_of_undying");
    public static List<String> SWEEP_LIFETIMES = List.of();
    public static List<String> KEEP_DURATIONS = List.of();
    public static List<String> COMPRESS_RULES = List.of();

    public static boolean ENABLE_VOID_PROTECT = true;
    public static int VOID_RAISE_MAX_STEPS = 6;
    public static boolean VOID_RESET_VELOCITY = true;

    public static String PREFIX = "<gradient:#00E5FF:#00FF7F>[SmartClean]</gradient>";
    public static List<Integer> WARN_SECONDS = Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1);

    public static String MSG_PRE_COLLECT = "即将在 {time}s 后回收世界掉落物，请及时拾取重要物品！";
    public static String MSG_COLLECTED = "已回收掉落物，共 {entities} 个实体。使用 /smartclean gui 领取，按物品独立倒计时清空。";
    public static String MSG_ITEM_SWEPT = "已按独立倒计时回收 {entities} 个掉落物。";
    public static String MSG_PURGED = "回收站有 {slots} 个槽位到期清空。";
    public static String MSG_STATUS = "回收站：{slotsUsed}/{slotsTotal} 槽（{pages} 页），物品 {items} 个；最近到期：{keep}；自动回收：{auto}，间隔：{interval}s，默认保留：{keepCfg}s，广播：{announce}。";
    public static String MSG_HELP = "<gradient:#7AD7F0:#4FC3F7>SmartClean 帮助</gradient>\\n<gradient:#A8FF78:#78FFD6>/smartclean gui</gradient> - 打开分页回收站\\n<gradient:#A8FF78:#78FFD6>/smartclean status</gradient> - 查看状态\\n<gradient:#FFD54F:#FF8A00>/smartclean collect</gradient> - 立即回收（权限2）\\n<gradient:#FFD54F:#FF8A00>/smartclean purge</gradient> - 立即清空（权限2）\\n<gradient:#B388FF:#7C4DFF>/smartclean reload</gradient> - 重载配置（权限2）";
    public static String MSG_CMD_COLLECT = "已回收掉落物，共 {entities} 个实体。可在回收站按物品独立倒计时领取。";
    public static String MSG_CMD_COLLECT_NONE = "没有可清理的掉落物！";
    public static String MSG_CMD_PURGE = "已清空回收站，清除了 {slots} 个槽位中的物品（黑名单物品已跳过）。";

    private static final String FILE_NAME = "smartclean-common.toml";

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) {
            writeDefaults(path);
            SmartClean.LOGGER.info("[SmartClean] 已生成默认配置 {}", path);
            return;
        }
        Map<String, String> map;
        try {
            map = parse(path);
        } catch (IOException e) {
            SmartClean.LOGGER.warn("[SmartClean] 读取配置失败，使用内存默认值：{}", e.toString());
            return;
        }

        ENABLE_AUTO_COLLECT = bool(map, "general.enableAutoCollect", ENABLE_AUTO_COLLECT);
        TICKS_PER_COLLECT = clamp(intVal(map, "general.ticksPerCollect", TICKS_PER_COLLECT), 20, 20 * 60 * 60);
        TICKS_KEEP_AFTER_COLLECT = clamp(intVal(map, "general.ticksKeepAfterCollect", TICKS_KEEP_AFTER_COLLECT), 0, 20 * 60 * 60);
        ANNOUNCE_MESSAGES = bool(map, "general.announceMessages", ANNOUNCE_MESSAGES);

        MAX_PAGES = clamp(intVal(map, "trash.maxPages", MAX_PAGES), 1, 64);
        PLAYER_INSERT = bool(map, "trash.playerInsert", PLAYER_INSERT);
        SHOW_SLOT_TIMERS = bool(map, "trash.showSlotTimers", SHOW_SLOT_TIMERS);

        ITEM_BLACKLIST = stringList(map, "blacklist.items", ITEM_BLACKLIST);
        SWEEP_LIFETIMES = stringList(map, "sweep_per_item.itemLifetimes", SWEEP_LIFETIMES);
        KEEP_DURATIONS = stringList(map, "retention_per_item.itemKeepDurations", KEEP_DURATIONS);
        COMPRESS_RULES = stringList(map, "compress.rules", COMPRESS_RULES);

        ENABLE_VOID_PROTECT = bool(map, "void_protection.enableVoidProtect", ENABLE_VOID_PROTECT);
        VOID_RAISE_MAX_STEPS = clamp(intVal(map, "void_protection.raiseMaxSteps", VOID_RAISE_MAX_STEPS), 1, 32);
        VOID_RESET_VELOCITY = bool(map, "void_protection.resetVelocity", VOID_RESET_VELOCITY);

        PREFIX = string(map, "messages.prefix", PREFIX);
        WARN_SECONDS = intList(map, "messages.warnSeconds", WARN_SECONDS);

        MSG_PRE_COLLECT = string(map, "messages.msgPreCollect", MSG_PRE_COLLECT);
        MSG_COLLECTED = string(map, "messages.msgCollected", MSG_COLLECTED);
        MSG_ITEM_SWEPT = string(map, "messages.msgItemSwept", MSG_ITEM_SWEPT);
        MSG_PURGED = string(map, "messages.msgPurged", MSG_PURGED);
        MSG_STATUS = string(map, "messages.msgStatus", MSG_STATUS);
        MSG_HELP = string(map, "messages.msgHelp", MSG_HELP);
        MSG_CMD_COLLECT = string(map, "messages.msgCmdCollect", MSG_CMD_COLLECT);
        MSG_CMD_COLLECT_NONE = string(map, "messages.msgCmdCollectNone", MSG_CMD_COLLECT_NONE);
        MSG_CMD_PURGE = string(map, "messages.msgCmdPurge", MSG_CMD_PURGE);

        SmartClean.LOGGER.info("[SmartClean] 配置已加载：{}", path);
    }

    // ------------------------------------------------------------------
    // TOML 读写
    // ------------------------------------------------------------------

    private static Map<String, String> parse(Path path) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        String section = "";
        for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).strip();
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            String key = line.substring(0, eq).strip();
            String value = line.substring(eq + 1).strip();
            int hash = value.indexOf(" #");
            if (hash > 0) value = value.substring(0, hash).strip();
            map.put((section.isEmpty() ? "" : section + ".") + key, value);
        }
        return map;
    }

    private static void writeDefaults(Path path) {
        StringBuilder b = new StringBuilder();
        b.append("# SmartClean 配置文件（修改后执行 /smartclean reload 或重启生效）\n\n");

        b.append("[general]\n");
        b.append("# 是否启用周期性自动回收世界掉落物。\n");
        b.append("enableAutoCollect = ").append(ENABLE_AUTO_COLLECT).append('\n');
        b.append("# 两次自动回收之间的间隔（tick）。20 tick = 1 秒。\n");
        b.append("ticksPerCollect = ").append(TICKS_PER_COLLECT).append('\n');
        b.append("# 回收站内物品的默认保留时间（tick）。20 tick = 1 秒。0 表示永不自动清空。\n");
        b.append("ticksKeepAfterCollect = ").append(TICKS_KEEP_AFTER_COLLECT).append('\n');
        b.append("# 是否向在线玩家广播提示消息。\n");
        b.append("announceMessages = ").append(ANNOUNCE_MESSAGES).append("\n\n");

        b.append("[trash]\n");
        b.append("# 回收站最多保留多少页（每页 54 格）。\n");
        b.append("maxPages = ").append(MAX_PAGES).append('\n');
        b.append("# 是否允许玩家把物品主动放进回收站槽位。\n");
        b.append("playerInsert = ").append(PLAYER_INSERT).append('\n');
        b.append("# 是否在回收站槽位左上角显示该物品的剩余保留时间。\n");
        b.append("showSlotTimers = ").append(SHOW_SLOT_TIMERS).append("\n\n");

        b.append("[blacklist]\n");
        b.append("# 物品黑名单（形如 minecraft:nether_star）。\n");
        b.append("items = ").append(stringArray(ITEM_BLACKLIST)).append("\n\n");

        b.append("[sweep_per_item]\n");
        b.append("# 世界侧独立回收倒计时：物品ID,秒数。\n");
        b.append("itemLifetimes = ").append(stringArray(SWEEP_LIFETIMES)).append("\n\n");

        b.append("[retention_per_item]\n");
        b.append("# 回收站侧独立保留倒计时：物品ID,秒数；0 表示永不清空。\n");
        b.append("itemKeepDurations = ").append(stringArray(KEEP_DURATIONS)).append("\n\n");

        b.append("[compress]\n");
        b.append("# 压缩规则：源物品ID,源数量,目标物品ID,目标数量。\n");
        b.append("rules = ").append(stringArray(COMPRESS_RULES)).append("\n\n");

        b.append("[void_protection]\n");
        b.append("enableVoidProtect = ").append(ENABLE_VOID_PROTECT).append('\n');
        b.append("raiseMaxSteps = ").append(VOID_RAISE_MAX_STEPS).append('\n');
        b.append("resetVelocity = ").append(VOID_RESET_VELOCITY).append("\n\n");

        b.append("[messages]\n");
        b.append("prefix = ").append(quote(PREFIX)).append('\n');
        b.append("warnSeconds = ").append(intArray(WARN_SECONDS)).append('\n');
        b.append("msgPreCollect = ").append(quote(MSG_PRE_COLLECT)).append('\n');
        b.append("msgCollected = ").append(quote(MSG_COLLECTED)).append('\n');
        b.append("msgItemSwept = ").append(quote(MSG_ITEM_SWEPT)).append('\n');
        b.append("msgPurged = ").append(quote(MSG_PURGED)).append('\n');
        b.append("msgStatus = ").append(quote(MSG_STATUS)).append('\n');
        b.append("msgHelp = ").append(quote(MSG_HELP)).append('\n');
        b.append("msgCmdCollect = ").append(quote(MSG_CMD_COLLECT)).append('\n');
        b.append("msgCmdCollectNone = ").append(quote(MSG_CMD_COLLECT_NONE)).append('\n');
        b.append("msgCmdPurge = ").append(quote(MSG_CMD_PURGE)).append('\n');

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, b.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            SmartClean.LOGGER.warn("[SmartClean] 写入默认配置失败：{}", e.toString());
        }
    }

    private static String quote(String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    private static String stringArray(List<String> values) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) b.append(", ");
            b.append(quote(values.get(i)));
        }
        return b.append(']').toString();
    }

    private static String intArray(List<Integer> values) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) b.append(", ");
            b.append(values.get(i));
        }
        return b.append(']').toString();
    }

    // ------------------------------------------------------------------
    // 取值
    // ------------------------------------------------------------------

    private static boolean bool(Map<String, String> map, String key, boolean def) {
        String v = map.get(key);
        return v == null ? def : "true".equalsIgnoreCase(v.strip());
    }

    private static int intVal(Map<String, String> map, String key, int def) {
        String v = map.get(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.strip());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String string(Map<String, String> map, String key, String def) {
        String v = map.get(key);
        return v == null ? def : unquote(v);
    }

    private static List<String> stringList(Map<String, String> map, String key, List<String> def) {
        String v = map.get(key);
        if (v == null) return def;
        List<String> out = new ArrayList<>();
        for (String part : splitArray(v)) {
            out.add(unquote(part));
        }
        return out;
    }

    private static List<Integer> intList(Map<String, String> map, String key, List<Integer> def) {
        String v = map.get(key);
        if (v == null) return def;
        List<Integer> out = new ArrayList<>();
        for (String part : splitArray(v)) {
            try {
                out.add(Integer.parseInt(part.strip()));
            } catch (NumberFormatException ignored) {
                // 跳过坏值
            }
        }
        return out;
    }

    private static List<String> splitArray(String raw) {
        String v = raw.strip();
        if (v.startsWith("[")) v = v.substring(1);
        if (v.endsWith("]")) v = v.substring(0, v.length() - 1);
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        boolean escape = false;
        for (char c : v.toCharArray()) {
            if (escape) {
                cur.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                cur.append(c);
                escape = true;
                continue;
            }
            if (c == '"') {
                inQuote = !inQuote;
                cur.append(c);
                continue;
            }
            if (c == ',' && !inQuote) {
                out.add(cur.toString().strip());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (!cur.toString().isBlank()) out.add(cur.toString().strip());
        return out;
    }

    private static String unquote(String raw) {
        String v = raw.strip();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        StringBuilder out = new StringBuilder();
        boolean escape = false;
        for (char c : v.toCharArray()) {
            if (escape) {
                out.append(c == 'n' ? '\n' : c == 't' ? '\t' : c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            out.append(c);
        }
        if (escape) out.append('\\');
        return out.toString();
    }

    private SCConfig() {}
}
