package haage.gui_time;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class GUITimeClient implements ClientModInitializer {
    private static final int FRAME_COUNT = 64;
    private static final Identifier[] CLOCK_FRAMES = new Identifier[FRAME_COUNT];
    private static final Identifier EXCL_ICON     = Identifier.of(GUITime.MOD_ID, "textures/gui/exclamation.png");
    private static final Identifier EXCL_ICON2    = Identifier.of(GUITime.MOD_ID, "textures/gui/exclamation_2.png");

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            CLOCK_FRAMES[i] = Identifier.of(
                    GUITime.MOD_ID,
                    String.format("textures/gui/clock_%02d.png", i)
            );
        }
    }

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tick) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            int sw = client.getWindow().getScaledWidth();
            int sh = client.getWindow().getScaledHeight();

            GuiTimeConfig cfg         = GuiTimeConfig.get();
            GuiTimeConfig.Corner corner = cfg.corner;
            GuiTimeConfig.DisplayMode mode = cfg.displayMode;

            // Precompute widths
            int textW = client.textRenderer.getWidth("00:00");
            boolean wantExcl = cfg.showSleepIndicator;
            int iconW = (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) ? 16 : 0;
            int exclW = wantExcl ? 6 : 0;
            int gap   = 2;

            // total group width
            int groupW = 0;
            if (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) groupW += iconW;
            if (exclW > 0)                                                                                  groupW += gap + exclW;
            if (mode == GuiTimeConfig.DisplayMode.TIME_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) groupW += gap + textW;

            // origin
            int groupX, groupY;
            switch (corner) {
                case TOP_LEFT:
                    groupX = 10;               groupY = 10;               break;
                case TOP_RIGHT:
                    groupX = sw - 10 - groupW; groupY = 10;               break;
                case BOTTOM_LEFT:
                    groupX = 10;               groupY = sh - 10 - 16;     break;
                default: // BOTTOM_RIGHT
                    groupX = sw - 10 - groupW; groupY = sh - 10 - 16;     break;
            }

            // clock frame
            long dayTicks = client.world.getTimeOfDay() % 24000L;
            float partial = tick.getTickProgress(false);
            float prog    = (dayTicks + partial) / 24000f;
            int frame     = ((int)(prog * FRAME_COUNT)) & (FRAME_COUNT - 1);

            boolean isRight  = (corner == GuiTimeConfig.Corner.TOP_RIGHT ||
                    corner == GuiTimeConfig.Corner.BOTTOM_RIGHT);
            boolean canSleep = (dayTicks >= 12541L && dayTicks <= 23458L);
            boolean warnSoon = (dayTicks >= 11541L && dayTicks < 12541L);

            // draw sequence
            int x = groupX;
            if (!isRight) {
                // left side: clock -> (warnSoon?icon2:sleep?icon1) -> time
                if (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    ctx.drawTexture(RenderLayer::getGuiTexturedOverlay, CLOCK_FRAMES[frame], x, groupY, 0f,0f,16,16,16,16);
                    x += iconW + gap;
                }
                if (wantExcl) {
                    if (warnSoon) {
                        ctx.drawTexture(RenderLayer::getGuiTexturedOverlay, EXCL_ICON2, x, groupY, 0f,0f,exclW,16,exclW,16);
                    } else if (canSleep) {
                        ctx.drawTexture(RenderLayer::getGuiTexturedOverlay, EXCL_ICON,  x, groupY, 0f,0f,exclW,16,exclW,16);
                    }
                }
                x += exclW + gap;
                if (mode == GuiTimeConfig.DisplayMode.TIME_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    String timeText = String.format(Locale.ROOT, "%02d:%02d",
                            (int)(((dayTicks/1000f)+6f)%24f),
                            (int)((((dayTicks/1000f)+6f)%1f)*60f)
                    );
                    ctx.drawText(client.textRenderer, timeText, x, groupY + 4, 0xFFFFFF, true);
                }
            } else {
                // right side: time -> indicator -> clock
                if (mode == GuiTimeConfig.DisplayMode.TIME_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    String timeText = String.format(Locale.ROOT, "%02d:%02d",
                            (int)(((dayTicks/1000f)+6f)%24f),
                            (int)((((dayTicks/1000f)+6f)%1f)*60f)
                    );
                    ctx.drawText(client.textRenderer, timeText, x, groupY + 4, 0xFFFFFF, true);
                    x += textW + gap;
                }
                if (wantExcl) {
                    if (warnSoon) {
                        ctx.drawTexture(RenderLayer::getGuiTexturedOverlay, EXCL_ICON2, x, groupY, 0f,0f,exclW,16,exclW,16);
                    } else if (canSleep) {
                        ctx.drawTexture(RenderLayer::getGuiTexturedOverlay, EXCL_ICON,  x, groupY, 0f,0f,exclW,16,exclW,16);
                    }
                }
                x += exclW + gap;
                if (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    ctx.drawTexture(RenderLayer::getGuiTexturedOverlay, CLOCK_FRAMES[frame], x, groupY, 0f,0f,16,16,16,16);
                }
            }
        });
    }
}
