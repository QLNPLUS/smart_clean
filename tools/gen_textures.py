"""程序化生成 SmartClean 的 GUI 贴图与模组图标（不依赖任何外部素材）。

界面背景直接使用原版箱子贴图 minecraft:textures/gui/container/generic_54.png，
这里只生成原版没有的两类资源：

  page_buttons.png   28x96    左右翻页按钮（列 0 左 / 列 1 右；行 0 普通 / 1 悬停 / 2 禁用）
  icon.png           128x128 模组列表图标

配色取自原版 GUI 调色板（面 #C6C6C6、边框黑、高光白、阴影 #555555），
与箱子界面放在一起不违和。

用法：
  python tools/gen_textures.py <输出目录>
"""

import sys
import os
from PIL import Image, ImageDraw

# 原版 GUI 调色板
BTN_BODY = (198, 198, 198, 255)
BTN_BODY_HOVER = (214, 214, 214, 255)
BTN_BODY_DISABLED = (139, 139, 139, 255)
BTN_BORDER = (0, 0, 0, 255)
BTN_TOP_HILIGHT = (255, 255, 255, 255)
BTN_BOTTOM_SHADOW = (85, 85, 101, 255)
ARROW_NORMAL = (64, 64, 64, 255)
ARROW_HOVER = (32, 32, 32, 255)
ARROW_DISABLED = (110, 110, 110, 255)

BTN_W = 14
BTN_H = 32


def arrow_pixels(cx, cy, left):
    pixels = []
    for i in range(5):
        half = 4 - i
        px = cx - i if left else cx + i
        pixels.append((px, cy - half, px + 1, cy + half + 1))
    return pixels


def draw_button(img, ox, oy, left, state):
    draw = ImageDraw.Draw(img)
    body = (BTN_BODY, BTN_BODY_HOVER, BTN_BODY_DISABLED)[state]
    arrow = (ARROW_NORMAL, ARROW_HOVER, ARROW_DISABLED)[state]

    # 外框 + 面
    draw.rectangle([ox, oy, ox + BTN_W - 1, oy + BTN_H - 1], fill=BTN_BORDER)
    draw.rectangle([ox + 1, oy + 1, ox + BTN_W - 2, oy + BTN_H - 2], fill=body)

    # 原版按钮的 1px 高光/阴影
    draw.line([(ox + 1, oy + 1), (ox + BTN_W - 2, oy + 1)], fill=BTN_TOP_HILIGHT)
    draw.line([(ox + 1, oy + 1), (ox + 1, oy + BTN_H - 2)], fill=BTN_TOP_HILIGHT)
    draw.line([(ox + 1, oy + BTN_H - 2), (ox + BTN_W - 2, oy + BTN_H - 2)], fill=BTN_BOTTOM_SHADOW)
    draw.line([(ox + BTN_W - 2, oy + 1), (ox + BTN_W - 2, oy + BTN_H - 2)], fill=BTN_BOTTOM_SHADOW)

    cx = ox + BTN_W // 2 + (1 if left else 0)
    cy = oy + BTN_H // 2
    for box in arrow_pixels(cx, cy, left):
        draw.rectangle(box, fill=arrow)


def build_buttons():
    img = Image.new("RGBA", (BTN_W * 2, BTN_H * 3), (0, 0, 0, 0))
    for state in range(3):
        draw_button(img, 0, state * BTN_H, True, state)
        draw_button(img, BTN_W, state * BTN_H, False, state)
    return img


def build_icon():
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 圆角底板（原版按钮灰）
    draw.rounded_rectangle([8, 8, 120, 120], radius=10, fill=BTN_BODY, outline=BTN_BORDER)
    draw.rounded_rectangle([9, 9, 119, 119], radius=9, outline=BTN_TOP_HILIGHT)

    metal = (150, 156, 162, 255)
    metal_dark = (104, 110, 116, 255)
    metal_hilight = (206, 210, 214, 255)

    # 桶身（上宽下窄）
    top_y, bottom_y = 58, 104
    for y in range(top_y, bottom_y + 1):
        t = (y - top_y) / (bottom_y - top_y)
        half = int(34 - 8 * t)
        draw.line([(64 - half, y), (64 + half, y)], fill=metal)
    draw.line([(30, top_y), (98, top_y)], fill=metal_hilight)
    draw.line([(26, bottom_y), (102, bottom_y)], fill=metal_dark)

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
    build_buttons().save(os.path.join(gui, "page_buttons.png"))
    build_icon().save(os.path.join(out, "icon.png"))
    print("written to", out)


if __name__ == "__main__":
    main()
