# SmartClean 1.0.2

Fixes Shift-click losing items inside the trash GUI.
修复回收站里「Shift 点击导致物品丢失」的问题。

## Downloads / 下载

| File | Loader | Minecraft | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.2.jar` | Forge 47.3.0 | 1.20.1 | 17 |
| `smartclean-1.21.1-neoforge-1.0.2.jar` | NeoForge 21.1.209 | 1.21.1 | 21 |
| `smartclean-26.1.2-neoforge-1.0.2.jar` | NeoForge 26.1.2.114 | 26.1.2 | 25 |

Client and server are both required. / 客户端与服务端均需安装。

## Fixes in 1.0.2 / 1.0.2 修复

- **Shift-click no longer deletes items**: quick-move (`Shift` + click) is now handled by the server only. The client-side trash view is read-only and hands out copies, so local prediction can no longer shrink the `ItemStack` that the server shares.
  **Shift 点击不再吞物品**：快速移动（`Shift` + 点击）只由服务端处理；客户端回收站视图只读且返回副本，本地预测无法再改动与服务端共享的那个 `ItemStack`。

## Included from 1.0.1 / 1.0.1 已包含

- Real paging (`trash.maxPages`), page arrows in a transparent side gutter that JEI never covers, correct 28x96 arrow texture sampling.
  真正的分页（`trash.maxPages`）、翻页箭头位于 JEI 不会覆盖的透明侧边带、按真实 28x96 采样贴图。
- Vanilla-style grabbing: the item follows the cursor, leftovers return when the inventory is full, a carried stack goes back on close.
  原版抓取语义：物品跟到光标、背包满时退回、关界面时光标物品收回背包。
- Config in `config/smartclean-common.toml`, new `trash.playerInsert` (default `false`) to allow players to put items into the trash.
  配置在 `config/smartclean-common.toml`，新增 `trash.playerInsert`（默认关）允许玩家主动放入。
- Per-slot countdown text removed from the trash GUI.
  移除回收站槽位上的倒计时文字。

## Notes / 说明

- Unofficial branch built on [ClearDrops](https://github.com/YuWan-030/cleardrops) by YuWan, licensed CC BY 4.0. See `NOTICE.md`.
  基于 YuWan 的 ClearDrops 开发的非官方分支，遵循 CC BY 4.0，署名见 `NOTICE.md`。
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/smart-clean
- MC百科: SmartClean（智能清理）
