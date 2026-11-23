package dev.qln.smartclean.storage;

import net.minecraft.resources.ResourceLocation;

/**
 * 物品压缩规则：当回收站内 from 物品达到 fromCount 个时，将其替换为 to 物品 toCount 个。
 * 配置格式为 {@code 源物品ID,源数量,目标物品ID,目标数量}。
 */
public record CompressRule(ResourceLocation from, int fromCount, ResourceLocation to, int toCount) {

    public static CompressRule parse(String raw) {
        if (raw == null) return null;
        String line = raw.trim();
        if (line.isEmpty()) return null;
        String[] parts = line.split(",");
        if (parts.length != 4) return null;
        try {
            ResourceLocation from = parseId(parts[0]);
            ResourceLocation to = parseId(parts[2]);
            if (from == null || to == null) return null;
            int fromCount = Integer.parseInt(parts[1].trim());
            int toCount = Integer.parseInt(parts[3].trim());
            if (fromCount <= 0 || toCount <= 0) return null;
            // 自反规则若会增大数量，会形成无限循环，直接拒绝
            if (from.equals(to) && toCount >= fromCount) return null;
            return new CompressRule(from, fromCount, to, toCount);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static ResourceLocation parseId(String raw) {
        if (raw == null) return null;
        String id = raw.trim();
        if (id.isEmpty() || !id.contains(":")) return null;
        try {
            return ResourceLocation.tryParse(id);
        } catch (Exception e) {
            return null;
        }
    }
}
