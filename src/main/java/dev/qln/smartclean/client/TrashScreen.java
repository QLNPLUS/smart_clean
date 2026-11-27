package dev.qln.smartclean.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.qln.smartclean.config.SCConfig;
import dev.qln.smartclean.menu.TrashMenu;
import dev.qln.smartclean.net.SCNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 分页回收站界面：左右两侧绘制翻页按钮（不占用任何容器槽位），Esc 直接关闭。
 * 每个槽位左上角显示该物品独立的剩余清理时间。
 *
 * <p>GUI 在箱子贴图左右各留出 {@link #SIDEBAR} 宽的<b>透明占位带</b>（不绘制任何像素），
 * 只是把界面范围加宽，让 JEI 等右侧面板按加宽后的界面自动避开，不再遮挡或抢走点击；
 * 翻页按钮贴在这两条空白带紧靠箱子贴图的内侧边缘，看起来就挂在界面边上。
 */
public class TrashScreen extends AbstractContainerScreen<TrashMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private static final ResourceLocation BUTTONS = new ResourceLocation("smartclean", "textures/gui/page_buttons.png");
    private static final int BTN_W = 14;
    private static final int BTN_H = 32;
    /** 翻页按钮贴图实际尺寸：2 列（左/右）× 2 行（普通/悬停）+ 1 行（禁用）。 */
    private static final int BTN_TEX_W = 28;
    private static final int BTN_TEX_H = 96;

    /** 字体层：物品与文字走不同批次，抬到显式 Z 层再画，保证文字压得住图标。 */
    private static final float TIMER_LAYER_Z = 300.0f;


    /** 箱子贴图两侧各留出的透明占位带宽度（不放任何像素，只用来加宽界面范围并容纳翻页按钮）。 */
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
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        this.renderTooltip(pose, mouseX, mouseY);
        renderSlotTimersOnTop(pose);
    }

    /** 槽位倒计时：物品先落屏，再在显式 Z 层画字，保证文字在图标之上。 */
    private void renderSlotTimersOnTop(PoseStack pose) {
        if (!SCConfig.SHOW_SLOT_TIMERS.get()) return;
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, TIMER_LAYER_Z);
        for (int slot = 0; slot < TrashMenu.TRASH_SLOTS; slot++) {
            int seconds = this.menu.getRemainingSeconds(slot);
            if (seconds < 0) continue;
            int col = slot % 9;
            int row = slot / 9;
            int x = this.leftPos + SIDEBAR + 8 + col * 18 + 1;
            int y = this.topPos + 18 + row * 18 + 1;
            this.font.drawShadow(pose, formatSeconds(seconds), (float) x, (float) y, 0xFFFFFFFF);
        }
        pose.popPose();
    }

    private static String formatSeconds(int seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h";
        if (seconds >= 120) return (seconds / 60) + "m";
        return seconds + "s";
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
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;


        blit(pose, TEXTURE, x + SIDEBAR, y, 0, 0, VANILLA_W, 125, 256, 256);
        blit(pose, TEXTURE, x + SIDEBAR, y + 125, 0, 126, VANILLA_W, VANILLA_H - 125, 256, 256);

        int buttonY = buttonTop();
        boolean canPrev = this.menu.getPage() > 0;
        boolean canNext = this.menu.getPage() < this.menu.getPageCount() - 1;
        int leftX = buttonLeftX();
        int rightX = buttonRightX();

        this.hoverLeft = canPrev && this.isInside(mouseX, mouseY, leftX, buttonY, BTN_W, BTN_H);
        this.hoverRight = canNext && this.isInside(mouseX, mouseY, rightX, buttonY, BTN_W, BTN_H);

        drawPageButton(pose, leftX, buttonY, canPrev, this.hoverLeft, true);
        drawPageButton(pose, rightX, buttonY, canNext, this.hoverRight, false);
    }

    private void drawPageButton(PoseStack pose, int x, int y, boolean enabled, boolean hovered, boolean left) {
        int u = left ? 0 : BTN_W;
        int v = !enabled ? BTN_H * 2 : (hovered ? BTN_H : 0);
        blit(pose, BUTTONS, x, y, u, v, BTN_W, BTN_H, BTN_TEX_W, BTN_TEX_H);
    }

    /** 1.19.2 仍用 PoseStack + 显式绑定纹理的方式贴图。 */
    private static void blit(PoseStack pose, ResourceLocation texture, int x, int y, int u, int v,
                             int width, int height, int texWidth, int texHeight) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(pose, x, y, 0, (float) u, (float) v, width, height, texWidth, texHeight);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        this.font.draw(pose, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 0x404040);
        this.font.draw(pose, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 0x404040);

        int right = SIDEBAR + VANILLA_W - 8;
        String pageText = (this.menu.getPage() + 1) + " / " + this.menu.getPageCount();
        int width = this.font.width(pageText);
        this.font.draw(pose, pageText, (float) (right - width), (float) this.titleLabelY, 0x606060);

        String items = "物品 " + this.menu.getTotalItems();
        int itemsWidth = this.font.width(items);
        this.font.draw(pose, items, (float) (right - itemsWidth), (float) this.inventoryLabelY, 0x606060);

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
        SCNetwork.CHANNEL.sendToServer(new SCNetwork.PageRequest(delta));
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
