#!/usr/bin/env python3
"""用 AgentHelper 驱动真实 Minecraft 客户端做 UI 回归（SmartClean 专用）。

背景
----
本仓库的开发客户端跑在远程会话里，外部进程无法用 SendInput/PostMessage 注入输入
（GetForegroundWindow 返回 0，注入点击后画面 diff 恒为 0）。可行的办法是借用外部
mod **AgentHelper**：它在游戏内起一个 HTTP 桥，把"真实鼠标点击 / 键盘 / 命令 / 截图"
暴露成 HTTP 接口，坐标一律是 GUI 像素。AgentHelper 是第三方 mod，不要修改它。

接入步骤（每个 worktree 各做一次）
--------------------------------
1. 把 AgentHelper 的 jar 放进 `<worktree>/run/mods/`
   （可复制自 `D:/DSH_workspace/MC_project/SRPG_MOD/neoforge-1.21.1/run/mods/`
   的 `agenthelper-neoforge-1.21.1-1.0.0.jar`；目前只有 NeoForge 1.21.1 版）。
2. 写 `<worktree>/run/config/agenthelper.json`：`{"port": 25590}`
   （避开 25589 的残留占用；换端口也要同步本脚本的 --port）。
3. `./gradlew runClient`（首次会先出无障碍引导，脚本会自动点掉）。
4. `python tools/drive_client.py --port 25590 status` 应返回 TitleScreen。

接口（AgentHelper 提供，本脚本封装）
------------------------------------
- `GET /status`            当前 screen 类名、inGame、窗口/GUI 尺寸、玩家坐标
- `GET /move?x=&y=`        移动指针（GUI 坐标）
- `GET /press?x=&y=&button=` 按下
- `GET /release?x=&y=&button=` 抬起
- `GET /key?code=<glfw>`   键盘（256=Esc、257=Enter、258=Tab、81=Q）
- `GET /command?cmd=<urlencoded>` 以玩家身份发送命令（需要世界允许作弊）
- `GET /screenshot`        返回 PNG（framebuffer 尺寸）

注意
----
- 单人世界默认**不允许作弊**，`/give`、`/gamemode` 会报 Unknown command。
  可在创建世界时勾选"允许作弊"，或退出世界后把 `run/saves/<世界>/level.dat` 里
  `Data.allowCommands` 改成 1（本仓库用 nbtlib 做过，改前先备份）。
- 清理客户端进程时按窗口标题前缀精确匹配（`Minecraft Forge*` / `Minecraft NeoForge*`），
  不要用"标题含 Minecraft"，否则会把标题里带 Minecraft 的浏览器窗口一起杀掉。
"""

import argparse
import json
import sys
import time
import urllib.parse
import urllib.request

DEFAULT_PORT = 25590


class Client:
    def __init__(self, port=DEFAULT_PORT, host="::1"):
        self.base = f"http://[{host}]:{port}"

    def _get(self, path):
        return urllib.request.urlopen(self.base + path, timeout=12).read()

    def status(self):
        return json.loads(self._get("/status").decode("utf-8", "ignore"))

    def screen(self):
        return self.status()["screen"].split(".")[-1]

    def click(self, x, y, button=0, pause=0.45):
        q = f"x={int(x)}&y={int(y)}&button={button}&modifiers=0"
        self._get("/move?" + q)
        time.sleep(0.05)
        self._get("/press?" + q)
        time.sleep(0.08)
        self._get("/release?" + q)
        time.sleep(pause)

    def key(self, code, pause=0.3):
        self._get(f"/key?code={code}")
        time.sleep(pause)

    def command(self, cmd, pause=0.4):
        self._get("/command?cmd=" + urllib.parse.quote(cmd))
        time.sleep(pause)

    def screenshot(self, path):
        data = self._get("/screenshot")
        with open(path, "wb") as f:
            f.write(data)
        return path


def wait_in_game(client, seconds=40):
    end = time.time() + seconds
    while time.time() < end:
        st = client.status()
        scr = st["screen"].split(".")[-1]
        if st["inGame"] and scr not in ("Loading", "Progress", "ReceivingLevel", "null"):
            return st
        time.sleep(1.2)
    return client.status()


def join_world(client):
    """从任意界面推进到世界内（创造/生存由世界设置决定）。"""
    scr = client.screen()
    gw = client.status()["window"]["guiWidth"]
    gh = client.status()["window"]["guiHeight"]

    # 首次启动的无障碍引导：底部居中的"继续"
    if scr == "AccessibilityOnboardingScreen":
        client.click(gw // 2, gh - 24)
        scr = client.screen()

    if scr == "TitleScreen":
        client.click(gw // 2, gh // 4 + 42, pause=0.9)   # 单人游戏
        scr = client.screen()

    if scr == "SelectWorldScreen":
        client.click(gw // 2, 65)                        # 列表里第一个世界
        client.click(gw // 2 - 80, gh - 42, pause=0.6)   # Play Selected
    elif scr == "CreateWorldScreen":
        # 无存档时会直接落到创建界面：底部按钮带一点偏移
        for dx in (0, -100, 100, -155):
            client.click(gw // 2 + dx, gh - 24)
            if client.screen() != "CreateWorldScreen":
                break
    return wait_in_game(client)


def selftest(client, slot_x, slot_y):
    """造掉落物 → 收集进回收站 → 打开界面 → 点第一格。"""
    st = client.status()
    if st["screen"].split(".")[-1] != "null":
        client.key(256)  # 先关掉当前界面

    client.command("/gamemode survival")
    client.command("/give @s minecraft:diamond 8")
    time.sleep(0.5)
    for _ in range(5):
        client.key(81)  # Q 丢弃（创造模式下是删除，所以先切生存）
    client.command("/gamemode creative")
    client.command("/smartclean collect")
    client.command("/smartclean gui")
    time.sleep(1.2)

    scr = client.screen()
    print("screen after gui:", scr)
    if scr != "TrashScreen":
        print("!! 回收站界面没打开，检查 /smartclean gui 权限或命令是否被拒绝")
        return False

    print(f"click slot0 at ({slot_x},{slot_y})")
    client.click(slot_x, slot_y)
    time.sleep(1.0)
    print("screen after click:", client.screen())
    return True


def default_slot_xy(client):
    """TrashScreen：imageWidth=220 / imageHeight=222，槽位 0 在 GUI 内 (30,18) 起 16x16。"""
    st = client.status()
    gui_w = st["window"]["guiWidth"]
    gui_h = st["window"]["guiHeight"]
    left = (gui_w - 220) // 2
    top = (gui_h - 222) // 2
    return left + 38, top + 26


def main():
    ap = argparse.ArgumentParser(description="驱动 AgentHelper 客户端")
    ap.add_argument("--port", type=int, default=DEFAULT_PORT)
    ap.add_argument("action", choices=["status", "join", "selftest", "click", "key", "cmd", "shot"],
                    help="要执行的动作")
    ap.add_argument("args", nargs="*", help="click: x y；key: code；cmd: 命令文本；shot: 输出 png 路径")
    a = ap.parse_args()
    c = Client(a.port)

    if a.action == "status":
        print(json.dumps(c.status(), ensure_ascii=False, indent=1))
    elif a.action == "join":
        print(json.dumps(join_world(c), ensure_ascii=False, indent=1))
    elif a.action == "selftest":
        if len(a.args) >= 2:
            x, y = int(a.args[0]), int(a.args[1])
        else:
            x, y = default_slot_xy(c)
        ok = selftest(c, x, y)
        sys.exit(0 if ok else 1)
    elif a.action == "click":
        x, y = int(a.args[0]), int(a.args[1])
        button = int(a.args[2]) if len(a.args) > 2 else 0
        c.click(x, y, button)
        print("screen:", c.screen())
    elif a.action == "key":
        c.key(int(a.args[0]))
        print("screen:", c.screen())
    elif a.action == "cmd":
        c.command(" ".join(a.args))
        print("sent")
    elif a.action == "shot":
        print(c.screenshot(a.args[0] if a.args else "shot.png"))


if __name__ == "__main__":
    main()
