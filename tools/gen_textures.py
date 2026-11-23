"""程序化生成 SmartClean 的 GUI 贴图与模组图标（不依赖任何外部素材）。

输出：
  trash_panel.png    176x222  分页回收站界面背景（含 54 + 27 + 9 个物品槽凹槽）
  page_buttons.png   28x96    左右翻页按钮贴图（列 0 左 / 列 1 右；行 0 普通 / 1 悬停 / 2 禁用）
  icon.png           128x128 模组列表图标

用法：
  python tools/gen_textures.py <输出目录>
"""

import sys
import os
from PIL import Image, ImageDraw

# 调色板（深灰蓝面板 + 冷灰槽位，和原版箱子界面协调）
PANEL_BG = (38, 38, 43, 255)
PANEL_TOP = (30, 30, 34, 255)
BORDER_DARK = (14, 14, 16, 255)
BORDER_LIGHT = (60, 60, 68, 255)
SLOT_BG = (23, 23, 26, 255)
SLOT_SHADOW = (12, 12, 14, 255)
SLOT_HILIGHT = (58, 58, 66, 255)

BTN_BODY = (62, 62, 70, 255)
BTN_BODY_HOVER = (92, 92, 104, 255)
BTN_BODY_DISABLED = (42, 42, 48, 255)
BTN_TOP_HILIGHT = (110, 110, 122, 255)
BTN_BOTTOM_SHADOW = (32, 32, 36, 255)
ARROW_NORMAL = (232, 232, 240, 255)
ARROW_HOVER = (255, 255, 255, 255)
ARROW_DISABLED = (110, 110, 120, 255)

IMAGE_W = 176
IMAGE_H = 222
SLOT_START_X = 8
SLOT_START_Y = 18
INV_START_Y = 139
HOTBAR_Y = 197


def draw_slot(draw, x, y):
    draw.rectangle([x, y, x + 15, y + 15], fill=SLOT_BG, outline=SLOT_SHADOW)
    draw.line([(x + 1, y + 1), (x + 14, y + 1)], fill=SLOT_SHADOW)
    draw.line([(x + 1, y + 1), (x + 1, y + 14)], fill=SLOT_SHADOW)
    draw.line([(x + 1, y + 14), (x + 14, y + 14)], fill=SLOT_HILIGHT)
    draw.line([(x + 14, y + 1), (x + 14, y + 14)], fill=SLOT_HILIGHT)


def build_panel():
    img = Image.new("RGBA", (IMAGE_W, IMAGE_H), PANEL_BG)
    draw = ImageDraw.Draw(img)

    # 标题带
    draw.rectangle([1, 1, IMAGE_W - 2, 17], fill=PANEL_TOP)
    draw.line([(1, 17), (IMAGE_W - 2, 17)], fill=BORDER_LIGHT)

    # 外框
    draw.rectangle([0, 0, IMAGE_W - 1, IMAGE_H - 1], outline=BORDER_DARK)
    draw.rectangle([1, 1, IMAGE_W - 2, IMAGE_H - 2], outline=BORDER_LIGHT)

    # 回收站 54 格（6 行 9 列）
    for row in range(6):
        for col in range(9):
            draw_slot(draw, SLOT_START_X + col * 18, SLOT_START_Y + row * 18)

    # 玩家背包 27 格
    for row in range(3):
        for col in range(9):
            draw_slot(draw, SLOT_START_X + col * 18, INV_START_Y + row * 18)

    # 快捷栏 9 格
    for col in range(9):
        draw_slot(draw, SLOT_START_X + col * 18, HOTBAR_Y)

    # 背包区与快捷栏之间的分隔线
    draw.line([(8, HOTBAR_Y - 3), (IMAGE_W - 9, HOTBAR_Y - 3)], fill=SLOT_SHADOW)
    return img


def arrow_pixels(dx, dy, left):
    """返回 5 列宽的箭头像素坐标（列 -> 上下半高）。"""
    pixels = []
    for i in range(5):
        half = 4 - i
        px = dx - i if left else dx + i
        pixels.append((px, dy - half, px + 1, dy + half + 1))
    return pixels


def draw_button(img, ox, oy, left, state):
    draw = ImageDraw.Draw(img)
    body = (BTN_BODY, BTN_BODY_HOVER, BTN_BODY_DISABLED)[state]
    arrow = (ARROW_NORMAL, ARROW_HOVER, ARROW_DISABLED)[state]

    draw.rectangle([ox, oy, ox + 13, oy + 31], fill=BORDER_DARK)
    draw.rectangle([ox + 1, oy + 1, ox + 12, oy + 30], fill=body)
    draw.line([(ox + 1, oy + 1), (ox + 12, oy + 1)], fill=BTN_TOP_HILIGHT)
    draw.line([(ox + 1, oy + 30), (ox + 12, oy + 30)], fill=BTN_BOTTOM_SHADOW)
    draw.line([(ox + 1, oy + 1), (ox + 1, oy + 30)], fill=BTN_TOP_HILIGHT)

    cx = ox + 6 + (1 if left else 0)
    cy = oy + 16
    for box in arrow_pixels(cx, cy, left):
        draw.rectangle(box, fill=arrow)


def build_buttons():
    img = Image.new("RGBA", (28, 96), (0, 0, 0, 0))
    for state in range(3):
        draw_button(img, 0, state * 32, True, state)
        draw_button(img, 14, state * 32, False, state)
    return img


def build_icon():
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 圆角底板
    draw.rounded_rectangle([8, 8, 120, 120], radius=10, fill=(38, 38, 43, 255), outline=(60, 60, 68, 255))

    metal = (154, 160, 166, 255)
    metal_dark = (110, 116, 122, 255)
    metal_hilight = (196, 202, 208, 255)

    # 桶身（上宽下窄的梯形，逐行绘制）
    top_y, bottom_y = 58, 104
    for y in range(top_y, bottom_y + 1):
        t = (y - top_y) / (bottom_y - top_y)
        half = int(34 - 8 * t)
        cx = 64
        draw.line([(cx - half, y), (cx + half, y)], fill=metal)
    draw.line([(30, top_y), (98, top_y)], fill=metal_hilight)
    draw.line([(26, bottom_y), (102, bottom_y)], fill=metal_dark)

    # 桶身竖条纹理
    for x in (46, 64, 82):
        draw.line([(x, top_y + 4), (x, bottom_y - 4)], fill=metal_dark)

    # 桶口与桶盖
    draw.rectangle([26, 48, 102, 57], fill=metal, outline=metal_dark)
    draw.line([(26, 48), (102, 48)], fill=metal_hilight)
    draw.rectangle([48, 38, 80, 47], fill=metal, outline=metal_dark)
    draw.line([(48, 38), (80, 38)], fill=metal_hilight)

    # 绿色回收下箭头
    green = (86, 200, 120, 255)
    green_dark = (46, 140, 78, 255)
    for i in range(11):
        width = 22 - i * 2
        x0 = 64 - width // 2
        draw.line([(x0, 64 + i), (x0 + width - 1, 64 + i)], fill=green)
    draw.rectangle([60, 74, 68, 88], fill=green_dark)
    return img


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "out"
    gui = os.path.join(out, "assets", "smartclean", "textures", "gui")
    os.makedirs(gui, exist_ok=True)
    build_panel().save(os.path.join(gui, "trash_panel.png"))
    build_buttons().save(os.path.join(gui, "page_buttons.png"))
    build_icon().save(os.path.join(out, "icon.png"))
    print("written to", out)


if __name__ == "__main__":
    main()
