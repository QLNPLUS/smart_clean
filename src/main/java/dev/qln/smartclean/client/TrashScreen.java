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
 *
 * <p>GUI 在箱子贴图左右各留出 {@link #SIDEBAR} 宽的<b>透明占位带</b>（不绘制任何像素），
 * 只是把界面范围加宽，让 JEI 等右侧面板按加宽后的界面自动避开，不再遮挡或抢走点击；
 * 翻页按钮贴在这两条空白带紧靠箱子贴图的内侧边缘，看起来就挂在界面边上。
 */
public class TrashScreen extends AbstractContainerScreen<TrashMenu> {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    private static final Identifier BUTTONS = Identifier.fromNamespaceAndPath("smartclean", "textures/gui/page_buttons.png");
    private static final int BTN_W = 14;
    private static final int BTN_H = 32;
    /** 翻页按钮贴图实际尺寸：2 列（左/右）× 2 行（普通/悬停）+ 1 行（禁用）。 */
    private static final int BTN_TEX_W = 28;
    private static final int BTN_TEX_H = 96;

    /** 箱子贴图两侧各留出的透明占位带宽度（不放任何像素，只用来加宽界面范围并容纳翻页按钮）。 */
    private static final int SIDEBAR = 22;
    private static final int VANILLA_W = 176;
    private static final int VANILLA_H = 222;

    public TrashScreen(TrashMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, VANILLA_W + SIDEBAR * 2, VANILLA_H);
        this.titleLabelX = SIDEBAR + 8;
        this.inventoryLabelX = SIDEBAR + 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private int buttonTop() {
        return this.topPos + (this.imageHeight - BTN_H) / 2;
    }

    private int buttonLeftX() {
        return this.leftPos + SIDEBAR - BTN_W;
    }

    private int buttonRightX() {
        return this.leftPos + SIDEBAR + VANILLA_W;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int x = this.leftPos;
        int y = this.topPos;


        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + SIDEBAR, y, 0.0F, 0.0F, VANILLA_W, 125, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + SIDEBAR, y + 125, 0.0F, 126.0F, VANILLA_W, VANILLA_H - 125, 256, 256);

        int buttonY = buttonTop();
        boolean canPrev = this.menu.getPage() > 0;
        boolean canNext = this.menu.getPage() < this.menu.getPageCount() - 1;
        int leftX = buttonLeftX();
        int rightX = buttonRightX();

        boolean hoverLeft = canPrev && isInside(mouseX, mouseY, leftX, buttonY, BTN_W, BTN_H);
        boolean hoverRight = canNext && isInside(mouseX, mouseY, rightX, buttonY, BTN_W, BTN_H);

        drawPageButton(graphics, leftX, buttonY, canPrev, hoverLeft, true);
        drawPageButton(graphics, rightX, buttonY, canNext, hoverRight, false);

    }

    private void drawPageButton(GuiGraphicsExtractor graphics, int x, int y, boolean enabled, boolean hovered, boolean left) {
        int u = left ? 0 : BTN_W;
        int v = !enabled ? BTN_H * 2 : (hovered ? BTN_H : 0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS, x, y, (float) u, (float) v, BTN_W, BTN_H, BTN_TEX_W, BTN_TEX_H);
    }

    /** 槽位倒计时画在物品之上（标签阶段在物品之后执行），白色带阴影保证压在图标上也清晰可读。 */
    private void renderSlotTimers(GuiGraphicsExtractor graphics) {
        for (int slot = 0; slot < TrashMenu.TRASH_SLOTS; slot++) {
            int seconds = this.menu.getRemainingSeconds(slot);
            if (seconds < 0) continue;
            int col = slot % 9;
            int row = slot / 9;
            int x = SIDEBAR + 8 + col * 18 + 1;
            int y = 18 + row * 18 + 1;
            graphics.text(this.font, formatSeconds(seconds), x, y, 0xFFFFFFFF, true);
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

        int right = SIDEBAR + VANILLA_W - 8;
        String pageText = (this.menu.getPage() + 1) + " / " + this.menu.getPageCount();
        int width = this.font.width(pageText);
        graphics.text(this.font, pageText, right - width, this.titleLabelY, 0x606060, false);

        String items = "物品 " + this.menu.getTotalItems();
        int itemsWidth = this.font.width(items);
        graphics.text(this.font, items, right - itemsWidth, this.inventoryLabelY, 0x606060, false);
        renderSlotTimers(graphics);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int buttonY = buttonTop();
            if (this.menu.getPage() > 0 && isInside(event.x(), event.y(), buttonLeftX(), buttonY, BTN_W, BTN_H)) {
                requestPage(-1);
                return true;
            }
            if (this.menu.getPage() < this.menu.getPageCount() - 1
                    && isInside(event.x(), event.y(), buttonRightX(), buttonY, BTN_W, BTN_H)) {
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
