package dev.qln.smartclean.client;

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
 */
public class TrashScreen extends AbstractContainerScreen<TrashMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private static final int BTN_W = 14;
    private static final int BTN_H = 32;
    private static final int BTN_GAP = 8;

    private boolean hoverLeft;
    private boolean hoverRight;

    public TrashScreen(TrashMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 125);
        graphics.blit(TEXTURE, x, y + 125, 0, 126, this.imageWidth, 96);

        int buttonY = this.topPos + (this.imageHeight - BTN_H) / 2;
        boolean canPrev = this.menu.getPage() > 0;
        boolean canNext = this.menu.getPage() < this.menu.getPageCount() - 1;
        int leftX = this.leftPos - BTN_GAP - BTN_W;
        int rightX = this.leftPos + this.imageWidth + BTN_GAP;

        this.hoverLeft = canPrev && this.isInside(mouseX, mouseY, leftX, buttonY, BTN_W, BTN_H);
        this.hoverRight = canNext && this.isInside(mouseX, mouseY, rightX, buttonY, BTN_W, BTN_H);

        drawPageButton(graphics, leftX, buttonY, canPrev, this.hoverLeft, true);
        drawPageButton(graphics, rightX, buttonY, canNext, this.hoverRight, false);

        renderSlotTimers(graphics);
    }

    private void drawPageButton(GuiGraphics graphics, int x, int y, boolean enabled, boolean hovered, boolean left) {
        int border = enabled ? 0xFF1E1E1E : 0xFF2B2B2B;
        int body = enabled ? (hovered ? 0xFF6E6E6E : 0xFF4A4A4A) : 0xFF2F2F2F;
        graphics.fill(x, y, x + BTN_W, y + BTN_H, border);
        graphics.fill(x + 1, y + 1, x + BTN_W - 1, y + BTN_H - 1, body);

        int cx = x + BTN_W / 2 + (left ? 1 : -1);
        int cy = y + BTN_H / 2;
        int arrowColor = enabled ? (hovered ? 0xFFFFFFFF : 0xFFE2E2E2) : 0xFF808080;
        for (int i = 0; i < 5; i++) {
            int half = 4 - i;
            int px = left ? cx - i : cx + i;
            graphics.fill(px, cy - half, px + 1, cy + half + 1, arrowColor);
        }
    }

    private void renderSlotTimers(GuiGraphics graphics) {
        for (int slot = 0; slot < TrashMenu.TRASH_SLOTS; slot++) {
            int seconds = this.menu.getRemainingSeconds(slot);
            if (seconds < 0) continue;
            int col = slot % 9;
            int row = slot / 9;
            int x = this.leftPos + 8 + col * 18 + 1;
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

        String pageText = (this.menu.getPage() + 1) + " / " + this.menu.getPageCount();
        int width = this.font.width(pageText);
        graphics.drawString(this.font, pageText, this.imageWidth - 8 - width, this.titleLabelY, 0x606060, false);

        String items = "物品 " + this.menu.getTotalItems();
        int itemsWidth = this.font.width(items);
        graphics.drawString(this.font, items, this.imageWidth - 8 - itemsWidth, this.inventoryLabelY, 0x606060, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int buttonY = this.topPos + (this.imageHeight - BTN_H) / 2;
            int leftX = this.leftPos - BTN_GAP - BTN_W;
            int rightX = this.leftPos + this.imageWidth + BTN_GAP;
            if (this.menu.getPage() > 0 && isInside(mouseX, mouseY, leftX, buttonY, BTN_W, BTN_H)) {
                requestPage(-1);
                return true;
            }
            if (this.menu.getPage() < this.menu.getPageCount() - 1
                    && isInside(mouseX, mouseY, rightX, buttonY, BTN_W, BTN_H)) {
                requestPage(1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void requestPage(int delta) {
        SCNetwork.CHANNEL.sendToServer(new SCNetwork.PageRequest(delta));
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
