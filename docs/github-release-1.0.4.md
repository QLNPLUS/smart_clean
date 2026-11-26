# SmartClean 1.0.4

Fixes the layering of the optional per-slot countdown text.
修复可选槽位倒计时文字的层级。

## Downloads / 下载

| File | Loader | Minecraft | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.4.jar` | Forge 47.3.0 | 1.20.1 | 17 |
| `smartclean-1.21.1-neoforge-1.0.4.jar` | NeoForge 21.1.209 | 1.21.1 | 21 |
| `smartclean-26.1.2-neoforge-1.0.4.jar` | NeoForge 26.1.2.114 | 26.1.2 | 25 |
| `smartclean-26.2-neoforge-1.0.4.jar` | NeoForge 26.2.0.95 | 26.2 | 25 |

Client and server are both required. / 客户端与服务端均需安装。

## Fixes in 1.0.4 / 1.0.4 修复

- **Countdown text sits between the item icons and the tooltip.** Forge/NeoForge 1.20.1–1.21.1 draw it after `renderTooltip` on an explicit `z=300` layer: above item icons (150) and count decorations (200), below tooltips (400). 26.1.2/26.2 keep the extractor `nextStratum` path.
  **倒计时文字位于物品图标与 tooltip 之间**：1.20.1～1.21.1 在 `renderTooltip` 之后、`z=300` 的显式层绘制，高于物品（150）与数量装饰（200），低于 tooltip（400）；26.1.2/26.2 沿用提取器的 `nextStratum`。
- Slots whose item is configured to never expire (`0` or negative retention) still show no countdown at all — intentionally blank.
  配置为永不过期（保留时间 0 或负数）的物品槽位依然不显示任何倒计时文字，保持空白。

## Included from 1.0.3 / 1.0.3 已包含

- `trash.showSlotTimers` (default `false`): opt-in per-slot countdown text.
  `trash.showSlotTimers`（默认 `false`）：可选的槽位倒计时文字。
- The layering fix from `forge-gui-layering`: item icons live in a separate item buffer that used to flush after the batched text, which is why the countdown was always painted over. 1.20.1/1.21.1 now flush the base layer and item buffer first, then draw the text and flush the block; 26.x opens a new stratum.
  分层修正：物品图标走独立 item buffer、曾晚于批处理文字落屏，所以倒计时总被盖住。1.20.1～1.21.1 先冲刷基础层与物品再画字并在块内 flush；26.x 另起 stratum。
- Everything from 1.0.2: Shift-click fix, real paging, side page arrows JEI cannot cover, config in `config/smartclean-common.toml` with `trash.playerInsert`, NeoForge 26.2 build.
  1.0.2 的全部改动：Shift 点击修复、真正分页、JEI 不会覆盖的翻页箭头、`config/smartclean-common.toml` 中的配置与 `trash.playerInsert`、NeoForge 26.2 构建。

## Notes / 说明

- Unofficial branch built on [ClearDrops](https://github.com/YuWan-030/cleardrops) by YuWan, licensed CC BY 4.0. See `NOTICE.md`.
  基于 YuWan 的 ClearDrops 开发的非官方分支，遵循 CC BY 4.0，署名见 `NOTICE.md`。
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/smart-clean
- MC百科: SmartClean（智能清理）
