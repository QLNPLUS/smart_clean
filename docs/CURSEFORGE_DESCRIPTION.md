# SmartClean

SmartClean is an item cleaner and player-facing trash bin for Minecraft servers, built on top of [ClearDrops](https://github.com/YuWan-030/cleardrops) by YuWan (YuWan-030). It is an **unofficial fork** and is not affiliated with, endorsed by, or maintained by the original author.

## What it does

Dropped items are swept into a shared trash bin instead of being deleted. Players open the bin, browse it page by page, and take back what they still need. Everything that is not claimed in time gets removed — and you decide per item type how long that is.

## Features

- **Paged trash GUI.** The bin holds many pages of 54 slots each. Page arrows are drawn on the left and right side of the container screen, so no slot is wasted on a glass/button placeholder. `Esc` closes the screen as usual.
- **Per-item sweep countdown.** Give any item ID its own lifetime in the world (e.g. cobblestone disappears after 180s) instead of waiting for the global cleanup cycle. These items are swept first, on their own timer.
- **Per-item retention countdown.** Give any item ID its own retention time inside the bin (e.g. diamonds stay 600s while everything else clears in 120s). Each slot counts down independently, with retention priority over the global setting.
- **Trash compression.** When enough of item A piles up in the bin, it is automatically converted into item B — for example 8 oak logs become 6 oak planks. Rules are written as `source_id,source_amount,target_id,target_amount`.
- **Scheduled cleanup with warnings.** Configure the interval, and broadcast warnings at 60/30/10/5… seconds before each sweep.
- **Blacklist.** Items on the blacklist are never swept and never purged.
- **Void protection.** Items that fall below the world floor are lifted back to a safe position instead of vanishing.
- **Gradient chat messages.** Message templates support `<gradient:#RRGGBB:#RRGGBB>text</gradient>`, colour-less placeholders such as `{time}`, `{entities}`, `{slots}` and `{keep}`, and hot reload.
- **Config hot reload.** Edit and save `config/smartclean-common.toml`, or run `/smartclean reload`.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/smartclean help` | everyone | Show the in-game help text |
| `/smartclean gui` | everyone | Open the paged trash bin |
| `/smartclean status` | everyone | Show slots, items, page count and next expiry |
| `/smartclean collect` | level 2 | Sweep the world now |
| `/smartclean purge` | level 2 | Empty the bin now (blacklisted items are kept) |
| `/smartclean reload` | level 2 | Re-apply the server config |

## Configuration

The file is generated on first start at `config/smartclean-common.toml` (earlier versions used `serverconfig/smartclean-server.toml`).

```toml
[general]
enableAutoCollect = true
ticksPerCollect = 6000        # sweep every 5 minutes
ticksKeepAfterCollect = 2400  # default retention: 2 minutes (0 = keep forever)
announceMessages = true

[trash]
maxPages = 8                  # pages of 54 slots

[blacklist]
items = ["minecraft:nether_star", "minecraft:totem_of_undying"]

[sweep_per_item]
# item_id,seconds — swept on its own timer, ahead of the global cycle
itemLifetimes = ["minecraft:cobblestone,180"]

[retention_per_item]
# item_id,seconds — its own retention time in the bin (0 = never auto-clear)
itemKeepDurations = ["minecraft:diamond,600"]

[compress]
# source_id,source_amount,target_id,target_amount
rules = ["minecraft:oak_log,8,minecraft:oak_planks,6"]
```

## Client requirement

The page arrows and the countdown labels are rendered by the client, so **players must install the mod on the client too**. A vanilla client cannot open the paged bin. Server-side sweeping, countdowns and compression still work without it, but players cannot browse the result.

## Notes

- Separate builds are published for each supported Minecraft / mod-loader combination — Forge 1.20.1, NeoForge 1.21.1, NeoForge 26.1.2 and NeoForge 26.2; pick the file matching your server.
- The bin is shared and global: every player sees the same pages. Take items early, they expire on their own timers.
- Compression rules only look at item IDs, not NBT/components. Use them deliberately — a rule that runs away (target amount ≥ source amount for the same item) is rejected at load time.
- Only one cleaner mod should manage drops on a server; running several cleaners at the same time leads to double sweeping.

## Credits

Built on the trash-bin and cleanup design of **ClearDrops** by YuWan (YuWan-030) — https://github.com/YuWan-030/cleardrops, released under CC BY 4.0. Thanks to the original author for the foundation this fork builds on.

Licensed under CC BY 4.0.

---

# SmartClean（中文）

SmartClean 是一个面向 Minecraft 服务端的掉落物清理与回收站模组，基于 YuWan（YuWan-030）的 [ClearDrops](https://github.com/YuWan-030/cleardrops) 派生而来。本项目是**非官方分支**，与原作者的立场无关，也不代表原作者的任何背书。

## 它做什么

地上的掉落物不会被直接删除，而是被扫进一个共享回收站。玩家打开回收站、逐页浏览、把还需要的东西取回去；没被及时取走的物品按各自的倒计时清掉——具体留多久，由你按物品类型决定。

## 特性

- **分页回收站界面。** 每页 54 格、可翻多页。翻页箭头画在容器界面左右两侧，不再用玻璃或占位物品当按钮，每页 54 格全部可用；`Esc` 照常关闭界面。
- **按物品 ID 的世界侧倒计时。** 可以给任意物品 ID 单独设定在世界上存在的时间（例如圆石 180 秒后被扫走），不必等全局清理周期——它们会优先按自己的计时被回收。
- **按物品 ID 的回收站保留倒计时。** 可以给任意物品 ID 单独设定在回收站里的保留时间（例如钻石留 600 秒、其它物品 120 秒）。每个槽位按自己的到期时间独立清空。
- **物品压缩。** 某种物品在回收站里堆够数量后自动转换成另一种物品，例如 8 个原木换成 6 个木板。规则写作 `源ID,源数量,目标ID,目标数量`。
- **定时清理与预警。** 可配置清理间隔，并在清理前 60/30/10/5… 秒逐档广播预警。
- **黑名单。** 黑名单物品不会被扫走，也不会被清空。
- **虚空保护。** 掉进虚空的物品会被拉回到安全位置，而不是直接消失。
- **渐变消息。** 消息模板支持 `<gradient:#RRGGBB:#RRGGBB>文本</gradient>`、`{time}`／`{entities}`／`{slots}`／`{keep}` 等占位符，并支持热重载。
- **配置热重载。** 编辑并保存 `config/smartclean-common.toml` 即生效，也可以执行 `/smartclean reload`。

## 命令

| 命令 | 权限 | 说明 |
| --- | --- | --- |
| `/smartclean help` | 所有人 | 显示帮助 |
| `/smartclean gui` | 所有人 | 打开分页回收站 |
| `/smartclean status` | 所有人 | 查看槽位、物品数、页数与最近到期时间 |
| `/smartclean collect` | 等级 2 | 立刻清理世界掉落物 |
| `/smartclean purge` | 等级 2 | 立刻清空回收站（黑名单物品保留） |
| `/smartclean reload` | 等级 2 | 重新应用服务端配置 |

## 配置

首次启动时会在 `config/smartclean-common.toml` 生成配置文件（早期版本用的是 `serverconfig/smartclean-server.toml`）。

```toml
[general]
enableAutoCollect = true
ticksPerCollect = 6000        # 每 5 分钟清理一次
ticksKeepAfterCollect = 2400  # 默认保留 2 分钟（0 = 永不清空）
announceMessages = true

[trash]
maxPages = 8                  # 回收站页数（每页 54 格）

[blacklist]
items = ["minecraft:nether_star", "minecraft:totem_of_undying"]

[sweep_per_item]
# 物品ID,秒数 —— 按自己的计时优先回收，早于全局周期
itemLifetimes = ["minecraft:cobblestone,180"]

[retention_per_item]
# 物品ID,秒数 —— 回收站内的独立保留时间（0 = 永不自动清空）
itemKeepDurations = ["minecraft:diamond,600"]

[compress]
# 源ID,源数量,目标ID,目标数量
rules = ["minecraft:oak_log,8,minecraft:oak_planks,6"]
```

## 客户端要求

翻页按钮与倒计时数字由客户端渲染，**玩家客户端也必须安装本模组**。纯原版客户端无法打开分页回收站；服务端的清理、倒计时与压缩逻辑仍然生效，但玩家没法浏览回收站。

## 注意事项

- 每个受支持的 Minecraft / 加载器组合分别发布独立构建，请挑选与你服务端匹配的那个文件。
- 回收站是全局共享的：所有玩家看到同样的页面。想要就早点取，物品按各自倒计时到期。
- 压缩规则只按物品 ID 匹配，不看 NBT／数据组件；请谨慎配置——会导致无限增长的自反规则（同一物品且目标数量 ≥ 源数量）在加载时会被直接拒绝。
- 同一服务端只应保留一个清理类模组；多个清理器同时运行会导致重复清扫。

## 鸣谢

本项目的回收站与清理设计基于 **ClearDrops**（作者 YuWan / YuWan-030）—— https://github.com/YuWan-030/cleardrops ，其以 CC BY 4.0 发布。感谢原作者打下的基础。

本项目同样以 CC BY 4.0 发布。
