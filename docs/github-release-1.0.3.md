# SmartClean 1.0.3

Optional per-slot countdown text, now actually drawn above the item icons.
槽位剩余时间改为可选开关，并且真正画在物品图标之上。

## Downloads / 下载

| File | Loader | Minecraft | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.3.jar` | Forge 47.3.0 | 1.20.1 | 17 |
| `smartclean-1.21.1-neoforge-1.0.3.jar` | NeoForge 21.1.209 | 1.21.1 | 21 |
| `smartclean-26.1.2-neoforge-1.0.3.jar` | NeoForge 26.1.2.114 | 26.1.2 | 25 |
| `smartclean-26.2-neoforge-1.0.3.jar` | NeoForge 26.2.0.95 | 26.2 | 25 |

Client and server are both required. / 客户端与服务端均需安装。

## What's new / 本版变化

- **`trash.showSlotTimers`（默认 `false`）**：打开后每个格子左上角显示该物品的剩余保留时间；默认关闭，物品图标不再被文字压住。
  New config `trash.showSlotTimers` (default `false`): when enabled, the slot's remaining retention time is drawn in its top-left corner. Off by default so icons stay unobstructed.
- **倒计时真正画在图标之上**：文字曾一直被物品图标盖住，原因不是坐标或透明度，而是冲刷时序——物品图标走独立的 item buffer，晚于批处理的 `drawString` 落屏。1.20.1 / 1.21.1 现在先把基础层与物品落屏，再在 `z=250` 的显式层画字并在块内 flush；26.1.2 / 26.2 的 `GuiGraphicsExtractor` 没有 z 轴，改用 `nextStratum` 另起一层。
  The countdown is now genuinely above the icons: the old text was always painted over because item icons live in a separate buffer that flushed after the batched text. Forge/NeoForge 1.20.1–1.21.1 now flush the base layer and item buffer first, then draw the text in an explicit `z=250` layer with an in-block flush; 26.x's extractor has no Z axis, so it opens a new stratum instead.
- **文字颜色**：改为不透明白色 `0xFFFFFFFF`（26.x 的文字颜色是完整 ARGB，六位色值会完全透明）。
  Text colour is now opaque white `0xFFFFFFFF` (26.x text colours are full ARGB; six-digit values render invisible).

## Included from 1.0.2 / 1.0.2 已包含

Shift-click 修复、真正分页、JEI 不会覆盖的翻页箭头、原版抓取语义、配置位于 `config/smartclean-common.toml` 且含 `trash.playerInsert`、NeoForge 26.2 构建。

## Notes / 说明

- Unofficial branch built on [ClearDrops](https://github.com/YuWan-030/cleardrops) by YuWan, licensed CC BY 4.0. See `NOTICE.md`.
  基于 YuWan 的 ClearDrops 开发的非官方分支，遵循 CC BY 4.0，署名见 `NOTICE.md`。
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/smart-clean
- MC百科: SmartClean（智能清理）
