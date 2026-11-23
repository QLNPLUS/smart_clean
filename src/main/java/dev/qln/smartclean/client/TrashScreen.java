package dev.qln.smartclean.client;

import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.net.SCNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * 26.1.2 分页回收站界面：在容器界面左右两侧绘制翻页按钮（不占用槽位），Esc 直接关闭。
 * 每个槽位左上角显示该物品独立的剩余清理时间。
 */
public class TrashScreen extends AbstractContainerScreen<TrashMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("smartclean", "textures/gui/trash_panel.png");

    private static final Identifier BUTTONS = Identifier.fromNamespaceAndPath("smartclean", "textures/gui/page_buttons.png");
    private static final int BTN_W = 14;
    private static final int BTN_H = 32;
    private static final int BTN_GAP = 8;

    public TrashScreen(TrashMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 222);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 176, 222);

        int buttonY = this.topPos + (this.imageHeight - BTN_H) / 2;
        boolean canPrev = this.menu.getPage() > 0;
        boolean canNext = this.menu.getPage() < this.menu.getPageCount() - 1;
        int leftX = this.leftPos - BTN_GAP - BTN_W;
        int rightX = this.leftPos + this.imageWidth + BTN_GAP;

        boolean hoverLeft = canPrev && isInside(mouseX, mouseY, leftX, buttonY, BTN_W, BTN_H);
        boolean hoverRight = canNext && isInside(mouseX, mouseY, rightX, buttonY, BTN_W, BTN_H);

        drawPageButton(graphics, leftX, buttonY, canPrev, hoverLeft, true);
        drawPageButton(graphics, rightX, buttonY, canNext, hoverRight, false);

        renderSlotTimers(graphics);
    }

    private void drawPageButton(GuiGraphicsExtractor graphics, int x, int y, boolean enabled, boolean hovered, boolean left) {
        int u = left ? 0 : 14;
        int v = !enabled ? 64 : (hovered ? 32 : 0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS, x, y, (float) u, (float) v, BTN_W, BTN_H, 28, 96);
    }

    private void renderSlotTimers(GuiGraphicsExtractor graphics) {
        for (int slot = 0; slot < TrashMenu.TRASH_SLOTS; slot++) {
            int seconds = this.menu.getRemainingSeconds(slot);
            if (seconds < 0) continue;
            int col = slot % 9;
            int row = slot / 9;
            int x = this.leftPos + 8 + col * 18 + 1;
            int y = this.topPos + 18 + row * 18 + 1;
            graphics.text(this.font, formatSeconds(seconds), x, y, 0xFFFFD166, true);
        }
    }

    private static String formatSeconds(int seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h";
        if (seconds >= 120) return (seconds / 60) + "m";
        return seconds + "s";
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        String pageText = (this.menu.getPage() + 1) + " / " + this.menu.getPageCount();
        int width = this.font.width(pageText);
        graphics.text(this.font, pageText, this.imageWidth - 8 - width, this.titleLabelY, 0x606060, false);

        String items = "物品 " + this.menu.getTotalItems();
        int itemsWidth = this.font.width(items);
        graphics.text(this.font, items, this.imageWidth - 8 - itemsWidth, this.inventoryLabelY, 0x606060, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int buttonY = this.topPos + (this.imageHeight - BTN_H) / 2;
            int leftX = this.leftPos - BTN_GAP - BTN_W;
            int rightX = this.leftPos + this.imageWidth + BTN_GAP;
            if (this.menu.getPage() > 0 && isInside(event.x(), event.y(), leftX, buttonY, BTN_W, BTN_H)) {
                requestPage(-1);
                return true;
            }
            if (this.menu.getPage() < this.menu.getPageCount() - 1
                    && isInside(event.x(), event.y(), rightX, buttonY, BTN_W, BTN_H)) {
                requestPage(1);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void requestPage(int delta) {
        ClientPacketDistributor.sendToServer(new SCNetwork.PageRequest(delta));
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
