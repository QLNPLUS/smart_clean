# SmartClean 1.0.5

Fixes two startup bugs on NeoForge 26.x and adds four new platform builds.

## What's new

- **NeoForge 26.3** (`smartclean-26.3-neoforge-1.0.5.jar`, NeoForge 26.3.0.118, Java 25)
- **Forge 1.19.2** (`smartclean-1.19.2-forge-1.0.5.jar`, Forge 43.4.0, Java 17)
- **Fabric 1.21.1** (`smartclean-1.21.1-fabric-1.0.5.jar`, Fabric Loader 0.16+, Fabric API required)
- **Fabric 1.20.1** (`smartclean-1.20.1-fabric-1.0.5.jar`, Fabric Loader 0.16+, Fabric API required)

## Fixes

- **NeoForge 26.2 / 26.3 crashed on server start**: the config was applied during mod construction, which throws `Cannot get config value before config is loaded`. It is now applied in `ModConfigEvent.Loading`. If you ran 26.2 or 26.3 on a dedicated server before, this was the crash.
- **NeoForge 26.x resource pack**: the pack format number is above 64 in 26.x, so `pack.mcmeta` must declare `min_format`/`max_format`. Both 26.2 and 26.3 now do.
- **NeoForge 26.3 config type**: FML 12 renamed `ModConfig.Type.COMMON`/`SERVER` to `LOCAL`/`SYNCED`; 26.3 registers as `LOCAL`. The file is still `config/smartclean-common.toml`.
- **Fabric 1.20.1**: `fabric.mod.json` declared `minecraft: 1.21.1` and was rejected by the loader; fixed to `1.20.1`.
- **Forge 1.19.2**: `SmartClean` also applied its config during construction; fixed the same way as 26.2/26.3.

## Files

| File | Minecraft | Loader | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.5.jar` | 1.20.1 | Forge 47.x | 17 |
| `smartclean-1.19.2-forge-1.0.5.jar` | 1.19.2 | Forge 43.x | 17 |
| `smartclean-1.21.1-neoforge-1.0.5.jar` | 1.21.1 | NeoForge 21.1.x | 21 |
| `smartclean-1.21.1-fabric-1.0.5.jar` | 1.21.1 | Fabric 0.16+ | 21 |
| `smartclean-1.20.1-fabric-1.0.5.jar` | 1.20.1 | Fabric 0.16+ | 17 |
| `smartclean-26.1.2-neoforge-1.0.5.jar` | 26.1.2 | NeoForge 26.1.2.x | 25 |
| `smartclean-26.2-neoforge-1.0.5.jar` | 26.2 | NeoForge 26.2.0.x | 25 |
| `smartclean-26.3-neoforge-1.0.5.jar` | 26.3 | NeoForge 26.3.0.x | 25 |

---

# SmartClean 1.0.5（中文）

修复 NeoForge 26.x 上的两个启动问题，并新增四套平台构建。

## 新增

- **NeoForge 26.3**（`smartclean-26.3-neoforge-1.0.5.jar`，NeoForge 26.3.0.118，Java 25）
- **Forge 1.19.2**（`smartclean-1.19.2-forge-1.0.5.jar`，Forge 43.4.0，Java 17）
- **Fabric 1.21.1**（`smartclean-1.21.1-fabric-1.0.5.jar`，需 Fabric Loader 0.16+ 与 Fabric API）
- **Fabric 1.20.1**（`smartclean-1.20.1-fabric-1.0.5.jar`，需 Fabric Loader 0.16+ 与 Fabric API）

## 修复

- **NeoForge 26.2 / 26.3 服务端启动崩溃**：配置在 mod 构造阶段被读取，抛出 `Cannot get config value before config is loaded`；改为在 `ModConfigEvent.Loading` 里应用。之前在 26.2/26.3 上开专用服务器崩的就是这个。
- **NeoForge 26.x 资源包**：26.x 的 pack 格式号超过 64，`pack.mcmeta` 必须声明 `min_format`/`max_format`，26.2 与 26.3 都已补上。
- **NeoForge 26.3 配置类型**：FML 12 把 `ModConfig.Type.COMMON`/`SERVER` 更名为 `LOCAL`/`SYNCED`，26.3 用 `LOCAL` 注册，文件仍是 `config/smartclean-common.toml`。
- **Fabric 1.20.1**：`fabric.mod.json` 的 `minecraft` 依赖写成了 1.21.1，被加载器拒绝；已改成 1.20.1。
- **Forge 1.19.2**：`SmartClean` 同样在构造阶段读配置，按相同方式修复。

## 文件

| 文件 | Minecraft | 加载器 | Java |
| --- | --- | --- | --- |
| `smartclean-1.20.1-forge-1.0.5.jar` | 1.20.1 | Forge 47.x | 17 |
| `smartclean-1.19.2-forge-1.0.5.jar` | 1.19.2 | Forge 43.x | 17 |
| `smartclean-1.21.1-neoforge-1.0.5.jar` | 1.21.1 | NeoForge 21.1.x | 21 |
| `smartclean-1.21.1-fabric-1.0.5.jar` | 1.21.1 | Fabric 0.16+ | 21 |
| `smartclean-1.20.1-fabric-1.0.5.jar` | 1.20.1 | Fabric 0.16+ | 17 |
| `smartclean-26.1.2-neoforge-1.0.5.jar` | 26.1.2 | NeoForge 26.1.2.x | 25 |
| `smartclean-26.2-neoforge-1.0.5.jar` | 26.2 | NeoForge 26.2.0.x | 25 |
| `smartclean-26.3-neoforge-1.0.5.jar` | 26.3 | NeoForge 26.3.0.x | 25 |

注意：客户端必须也安装本模组，翻页箭头与可选倒计时由客户端绘制。
