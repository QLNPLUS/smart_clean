# SmartClean 1.0.0

Server-side dropped-item cleanup with a paged trash GUI, per-item countdowns and auto compression.
服务端掉落物清理：分页回收站界面、按物品独立倒计时与自动压缩。

## Downloads / 下载

| File | Loader | Minecraft | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.0.jar` | Forge 47.3.0 | 1.20.1 | 17 |
| `smartclean-1.21.1-neoforge-1.0.0.jar` | NeoForge 21.1.209 | 1.21.1 | 21 |
| `smartclean-26.1.2-neoforge-1.0.0.jar` | NeoForge 26.1.2.114 | 26.1.2 | 25 |

Client and server are both required. / 客户端与服务端均需安装。

## Highlights / 主要特性

- Paged trash GUI: 54 usable slots per page, left/right page buttons drawn beside the container, `Esc` closes directly — no placeholder glass panes.
  分页回收站：每页 54 格全部可用，翻页按钮绘制在容器两侧，`Esc` 直接关闭，无需占位玻璃。
- Per-item-ID countdowns with priority over the global cleanup timer, for both world-side sweeping and trash retention.
  按物品 ID 的独立倒计时，优先级高于全局清理；世界侧扫除与回收站保留时间均支持。
- Compression rules such as `minecraft:egg,5,minecraft:turtle_egg:4` — compress automatically once the stack count is reached.
  压缩规则形如 `minecraft:egg,5,minecraft:turtle_egg:4`，数量攒满即自动压缩。
- Scheduled cleanup with advance warnings, item blacklist, void protection, gradient broadcast messages and `/smartclean` commands.
  定时清理与逐档预警、物品黑名单、虚空保护、渐变广播消息与 `/smartclean` 命令。

## Notes / 说明

- Unofficial branch built on [ClearDrops](https://github.com/YuWan-030/cleardrops) by YuWan, licensed CC BY 4.0. See `NOTICE.md`.
  基于 YuWan 的 ClearDrops 开发的非官方分支，遵循 CC BY 4.0，署名见 `NOTICE.md`。
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/smart-clean
- MC百科: SmartClean（智能清理）
