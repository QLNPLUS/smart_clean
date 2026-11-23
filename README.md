# SmartClean

基于 [ClearDrops](https://github.com/YuWan-030/cleardrops) 的掉落物清理与回收站模组，新增三项能力：

1. **分页回收站 GUI**：不再占用容器槽位放"玻璃/占位物品"来翻页，改为在回收站界面左右两侧绘制翻页按钮；`Esc` 直接关闭界面，每页 54 格全部用于物品。
2. **按物品 ID 的独立倒计时**：可为任意物品 ID 配置独立的世界侧回收倒计时与回收站内保留倒计时，优先级高于全局配置。
3. **回收站物品压缩**：按配置规则，当回收站内某物品达到指定数量时，自动替换为目标物品与数量（例如 8 个原木 → 6 个木板）。

## 版本与分支

同一份功能在三套独立工程中并行维护，每个版本一个 git worktree：

| 目录 | Minecraft | 加载器 | Java | 分支 |
| --- | --- | --- | --- | --- |
| `versions/1.20.1-forge` | 1.20.1 | Forge 47.x | 17 | `mc/1.20.1-forge` |
| `versions/1.21.1-neoforge` | 1.21.1 | NeoForge 21.1.x | 21 | `mc/1.21.1-neoforge` |
| `versions/26.1.2-neoforge` | 26.1.2 | NeoForge 26.1.2.x | 25 | `mc/26.1.2-neoforge` |

三个版本的功能集保持一致；因为 Minecraft / 加载器 API 差异较大，代码按版本独立维护，不做跨版本共享模块。

## 构建

在对应 worktree 内执行：

| 版本 | 构建命令 |
| --- | --- |
| `versions/1.20.1-forge` | `./gradlew build` |
| `versions/1.21.1-neoforge` | `./gradlew build --offline` |
| `versions/26.1.2-neoforge` | `./gradlew build --offline` |

三个版本均已在当前机器构建通过，产物位于各自 `build/libs/`：

- `smartclean-1.20.1-forge-2.0.0.jar`（Forge 1.20.1-47.3.0，JDK 17）
- `smartclean-1.21.1-neoforge-2.0.0.jar`（NeoForge 21.1.209，JDK 21）
- `smartclean-26.1.2-neoforge-2.0.0.jar`（NeoForge 26.1.2.114，JDK 25）

注意：当前网络下 `maven.neoforged.net` 不可达，NeoForge 版本依赖本机 Gradle 缓存，需要带 `--offline`；Forge 1.20.1 从 `maven.minecraftforge.net` 拉取 MCP 数据（该域名可达，首次构建需联网）。JDK 由 Gradle toolchain 指定：17 / 21 走系统安装，25 走 `~/.gradle/jdks` 下的 Zulu 25。

## 功能清单

- 定时回收世界掉落物（可配置间隔、可关闭），回收前按秒数预警
- 回收站多页存储（每页 54 格），左右翻页按钮 + 页码显示
- 按物品 ID 独立回收倒计时（世界侧）/ 独立保留倒计时（回收站侧）
- 回收站物品压缩规则（`源ID,源数量,目标ID,目标数量`）
- 物品黑名单、虚空物品保护
- 渐变消息前缀、占位符模板、配置热重载
- 命令：`/smartclean help|gui|status|collect|purge|reload`

## 客户端要求

本模组的翻页按钮与独立倒计时显示由客户端渲染，**客户端需要安装同一模组**。仅装服务端时服务端逻辑仍然生效，但玩家无法打开分页界面。

## 署名

上游项目 ClearDrops 由 YuWan（YuWan-030）以 CC BY 4.0 发布，本仓库为其派生作品，保留署名与相同许可，详见 [NOTICE.md](NOTICE.md)。
