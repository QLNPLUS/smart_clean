# SmartClean 1.0.1

Bug-fix release for the paged trash GUI and item handling.
分页回收站与物品操作的修复版本。

## Downloads / 下载

| File | Loader | Minecraft | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.1.jar` | Forge 47.3.0 | 1.20.1 | 17 |
| `smartclean-1.21.1-neoforge-1.0.1.jar` | NeoForge 21.1.209 | 1.21.1 | 21 |
| `smartclean-26.1.2-neoforge-1.0.1.jar` | NeoForge 26.1.2.114 | 26.1.2 | 25 |

Client and server are both required. / 客户端与服务端均需安装。

## Fixes / 修复

- **Pages actually work**: the GUI page count follows `trash.maxPages`, so pages 2..8 are reachable instead of always showing `1 / 1`.
  翻页真的能用了：页数跟随 `trash.maxPages`，第 2～8 页可以翻到，不再永远显示 `1 / 1`。
- **No more JEI overlap**: page arrows live in a transparent side gutter of the widened container screen, so JEI neither covers them nor swallows the clicks.
  不再与 JEI 打架：翻页箭头放进界面加宽后的透明侧边带，JEI 既不遮挡也不抢点击。
- **Correct arrow texture**: the 28x96 sheet is sampled with its real size (Forge 1.20.1 used to sample the wrong region and draw black blocks).
  贴图采样正确：按真实 28x96 尺寸取样，Forge 1.20.1 之前的黑块白条消失。
- **Vanilla-style grabbing**: clicking a trash slot takes the item onto the cursor and you place it yourself; leftovers go back when the inventory is full, and a carried stack returns to the inventory on close instead of dropping.
  原版抓取语义：点击槽位把物品拿到光标自己放；背包满时原样退回，关界面时光标物品收回背包而不是掉落。
- **Items no longer vanish on click**: fixed a client/server page mismatch where the client rendered page 1 while the server took items from another page.
  点击不再吞物品：修掉客户端渲染第一页、服务端却在另一页取物的错位。
- **Read-only client view**: local click prediction can no longer consume the shared trash store before the server takes the item.
  客户端视图只读：本地点击预测不再提前消耗与服务端共享的回收站存储。
- **Config in `config/`**: moved from `serverconfig` to `config/smartclean-common.toml`; new `trash.playerInsert` (default `false`) lets players put items into the trash themselves.
  配置移到 `config/`：不再需要进存档目录；新增 `trash.playerInsert`（默认关）。
- **Per-slot countdown text removed** from the trash GUI.
  移除回收站槽位上的倒计时文字显示。

## Notes / 说明

- Unofficial branch built on [ClearDrops](https://github.com/YuWan-030/cleardrops) by YuWan, licensed CC BY 4.0. See `NOTICE.md`.
  基于 YuWan 的 ClearDrops 开发的非官方分支，遵循 CC BY 4.0，署名见 `NOTICE.md`。
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/smart-clean
- MC百科: SmartClean（智能清理）
