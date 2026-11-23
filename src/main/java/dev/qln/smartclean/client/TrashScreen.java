package dev.qln.smartclean.client;

import net.neoforged.neoforge.network.PacketDistributor;

import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.net.SCNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 分页回收站界面：左右两侧绘制翻页按钮（不占用任何容器槽位），Esc 直接关闭。
 * 每个槽位左上角显示该物品独立的剩余清理时间。
 *
 * <p>GUI 在箱子贴图左右各加一条 {@link #SIDEBAR} 宽的延伸带，翻页按钮放在延伸带里。
 * 按钮位于 GUI 自身范围内，JEI 等右侧面板会按加宽后的界面自动避开，不会再遮挡或抢走点击。
 */
public class TrashScreen extends AbstractContainerScreen<TrashMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    private static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath("smartclean", "textures/gui/page_buttons.png");
    private static final int BTN_W = 14;
    private static final int BTN_H = 32;
    /** 翻页按钮贴图实际尺寸：2 列（左/右）× 2 行（普通/悬停）+ 1 行（禁用）。 */
    private static final int BTN_TEX_W = 28;
    private static final int BTN_TEX_H = 96;

    /** 箱子贴图两侧各留出的延伸带宽度（容纳翻页按钮）。 */
    private static final int SIDEBAR = 22;
    private static final int VANILLA_W = 176;
    private static final int VANILLA_H = 222;

    private boolean hoverLeft;
    private boolean hoverRight;

    public TrashScreen(TrashMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = VANILLA_W + SIDEBAR * 2;
        this.imageHeight = VANILLA_H;
        this.titleLabelX = SIDEBAR + 8;
        this.inventoryLabelX = SIDEBAR + 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private int buttonTop() {
        return this.topPos + (this.imageHeight - BTN_H) / 2;
    }

    private int buttonLeftX() {
        return this.leftPos + (SIDEBAR - BTN_W) / 2;
    }

    private int buttonRightX() {
        return this.leftPos + this.imageWidth - SIDEBAR + (SIDEBAR - BTN_W) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        drawSidebars(graphics, x, y);

        graphics.blit(TEXTURE, x + SIDEBAR, y, 0, 0, VANILLA_W, 125);
        graphics.blit(TEXTURE, x + SIDEBAR, y + 125, 0, 126, VANILLA_W, VANILLA_H - 125);

        int buttonY = buttonTop();
        boolean canPrev = this.menu.getPage() > 0;
        boolean canNext = this.menu.getPage() < this.menu.getPageCount() - 1;
        int leftX = buttonLeftX();
        int rightX = buttonRightX();

        this.hoverLeft = canPrev && this.isInside(mouseX, mouseY, leftX, buttonY, BTN_W, BTN_H);
        this.hoverRight = canNext && this.isInside(mouseX, mouseY, rightX, buttonY, BTN_W, BTN_H);

        drawPageButton(graphics, leftX, buttonY, canPrev, this.hoverLeft, true);
        drawPageButton(graphics, rightX, buttonY, canNext, this.hoverRight, false);

        renderSlotTimers(graphics);
    }

    /** 用原版箱子界面的色调把两侧延伸带填成同色，并描一圈边线，让按钮看起来属于界面本身。 */
    private void drawSidebars(GuiGraphics graphics, int x, int y) {
        int side = 0xFFC6C6C6;
        int line = 0xFF555555;
        int rightX = x + this.imageWidth - SIDEBAR;
        graphics.fill(x, y, x + SIDEBAR, y + this.imageHeight, side);
        graphics.fill(rightX, y, x + this.imageWidth, y + this.imageHeight, side);
        // 外框与内侧分隔线
        graphics.fill(x, y, x + 1, y + this.imageHeight, line);
        graphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, line);
        graphics.fill(x + SIDEBAR - 1, y, x + SIDEBAR, y + this.imageHeight, line);
        graphics.fill(rightX, y, rightX + 1, y + this.imageHeight, line);
    }

    private void drawPageButton(GuiGraphics graphics, int x, int y, boolean enabled, boolean hovered, boolean left) {
        int u = left ? 0 : BTN_W;
        int v = !enabled ? BTN_H * 2 : (hovered ? BTN_H : 0);
        graphics.blit(BUTTONS, x, y, u, v, BTN_W, BTN_H, BTN_TEX_W, BTN_TEX_H);
    }

    private void renderSlotTimers(GuiGraphics graphics) {
        for (int slot = 0; slot < TrashMenu.TRASH_SLOTS; slot++) {
            int seconds = this.menu.getRemainingSeconds(slot);
            if (seconds < 0) continue;
            int col = slot % 9;
            int row = slot / 9;
            int x = this.leftPos + SIDEBAR + 8 + col * 18 + 1;
            int y = this.topPos + 18 + row * 18 + 1;
            String text = formatSeconds(seconds);
            graphics.drawString(this.font, text, x, y, 0xFFFFD166, true);
        }
    }

    private static String formatSeconds(int seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h";
        if (seconds >= 120) return (seconds / 60) + "m";
        return seconds + "s";
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        int right = SIDEBAR + VANILLA_W - 8;
        String pageText = (this.menu.getPage() + 1) + " / " + this.menu.getPageCount();
        int width = this.font.width(pageText);
        graphics.drawString(this.font, pageText, right - width, this.titleLabelY, 0x606060, false);

        String items = "物品 " + this.menu.getTotalItems();
        int itemsWidth = this.font.width(items);
        graphics.drawString(this.font, items, right - itemsWidth, this.inventoryLabelY, 0x606060, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int buttonY = buttonTop();
            if (this.menu.getPage() > 0 && isInside(mouseX, mouseY, buttonLeftX(), buttonY, BTN_W, BTN_H)) {
                requestPage(-1);
                return true;
            }
            if (this.menu.getPage() < this.menu.getPageCount() - 1
                    && isInside(mouseX, mouseY, buttonRightX(), buttonY, BTN_W, BTN_H)) {
                requestPage(1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void requestPage(int delta) {
        PacketDistributor.sendToServer(new SCNetwork.PageRequest(delta));
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
