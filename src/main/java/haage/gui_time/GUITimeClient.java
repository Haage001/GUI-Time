package haage.gui_time;

import java.util.Locale;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GUITimeClient implements ClientModInitializer {
    private static final Identifier EXCL_ICON    = Identifier.of(GUITime.MOD_ID, "textures/gui/exclamation.png");
    private static final Identifier EXCL_ICON2   = Identifier.of(GUITime.MOD_ID, "textures/gui/exclamation_2.png");
    private static final Identifier PHANTOM_ICON = Identifier.of(GUITime.MOD_ID, "textures/gui/phantom.png");
    /** Local counter of ticks since last sleep, set by mixin */
    private static int ticksSinceRest = 0;

    /**
     * Called from a mixin when the server sends a fresh Statistics packet.
     */
    public static void setTicksSinceRest(int ticks) {
        ticksSinceRest = ticks;
    }

    @Override
    public void onInitializeClient() {
       /* // ─── Debug keybinding to trigger phantom icon for testing ─────────────
        KeyBinding testPhantomKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.guitime.test_phantom",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_P,
                        "category.guitime.debug"
                )
        ); */

        // ─── Increment our local counter every client tick ─────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
           /* if (testPhantomKey.wasPressed()) {
                int threshold = GuiTimeConfig.get().phantomThresholdTicks;
                ticksSinceRest = threshold;
                client.player.sendMessage(Text.literal("[GUI Time] Phantom test triggered!"), false);
            } */
            if (client.world != null && client.player != null) {
                if (client.player.isSleeping()) {
                    ticksSinceRest = 0;
                } else {
                    ticksSinceRest++;
                }
            }
        });


        // ─── HUD rendering ────────────────────────────────────────────────────
        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tick) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            // World/time state
            boolean inOverworld  = client.world.getRegistryKey().equals(World.OVERWORLD);
            long dayTicks        = client.world.getTimeOfDay() % 24000L;
            boolean isNight      = (dayTicks >= 12540L && dayTicks <= 23458L);
            boolean isThundering = client.world.isThundering();
            boolean isRaining    = client.world.isRaining();

            // Sleep logic
            boolean canSleep = isThundering ||
                    (isRaining && (dayTicks >= 12030L || dayTicks <= 0L)) ||
                    (!isRaining && !isThundering && isNight);
            boolean warnSoon = !canSleep && !isThundering && (
                    (isRaining && dayTicks >= 11030L && dayTicks < 12030L) ||
                            (!isRaining && dayTicks >= 11530L && dayTicks < 12540L)
            );

            GuiTimeConfig cfg            = GuiTimeConfig.get();
            GuiTimeConfig.Corner corner  = cfg.corner;
            GuiTimeConfig.DisplayMode mode = cfg.displayMode;

            int sw = client.getWindow().getScaledWidth();
            int sh = client.getWindow().getScaledHeight();

            TextRenderer tr   = client.textRenderer;
            int textW    = tr.getWidth("00:00");
            boolean wantExcl      = cfg.showSleepIndicator && inOverworld;
            int iconW            = (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) ? 16 : 0;
            int exclW            = wantExcl ? 6  : 0;
            int phantomW         = cfg.showPhantomIndicator ? 10 : 0;
            int gap              = 2;

            // Total group width
            int groupW = 0;
            if (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) groupW += iconW;
            if (cfg.showPhantomIndicator)                                                                     groupW += gap + phantomW;
            if (exclW > 0)                                                                                    groupW += gap + exclW;
            if (mode == GuiTimeConfig.DisplayMode.TIME_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH)       groupW += gap + textW;

            // Positioning
            int groupX, groupY;
            switch (corner) {
                case TOP_LEFT:     groupX = 10;                    groupY = 10;                break;
                case TOP_RIGHT:    groupX = sw - 10 - groupW;      groupY = 10;                break;
                case TOP_CENTER: groupX = (sw - groupW)/2; groupY = 10; break;
                case BOTTOM_LEFT:  groupX = 10;                    groupY = sh - 10 - 16;      break;
                default:           groupX = sw - 10 - groupW;      groupY = sh - 10 - 16;      break;
            }

            // Create a clock ItemStack with the current world time for proper texture display
            ItemStack clockStack = new ItemStack(Items.CLOCK);
            clockStack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(null, false));
            
            boolean right = (corner == GuiTimeConfig.Corner.TOP_RIGHT || corner == GuiTimeConfig.Corner.BOTTOM_RIGHT);
            int x = groupX;

            if (!right) {
                // Clock icon - render the actual in-game clock item
                if (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    ctx.drawItem(clockStack, x, groupY);
                    x += iconW + gap;
                }
                // Phantom indicator
                if (cfg.showPhantomIndicator && inOverworld && isNight && ticksSinceRest >= cfg.phantomThresholdTicks) {
                    ctx.drawTexture(RenderPipelines.GUI_TEXTURED, PHANTOM_ICON, x, groupY, 0, 0, phantomW, phantomW, phantomW, phantomW);
                    x += phantomW + gap;
                }
                // Sleep indicator
                if (wantExcl) {
                    if (warnSoon) {
                        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, EXCL_ICON2, x, groupY, 0, 0, exclW, 16, exclW, 16);
                    } else if (canSleep) {
                        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, EXCL_ICON, x, groupY, 0, 0, exclW, 16, exclW, 16);
                    }
                }
                x += exclW + gap;
                // Digital time
                if (mode == GuiTimeConfig.DisplayMode.TIME_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    String timeText = String.format(Locale.ROOT, "%02d:%02d",
                            (int)(((dayTicks / 1000f) + 6f) % 24f),
                            (int)((((dayTicks / 1000f) + 6f) % 1f) * 60f)
                    );
                    ctx.drawText(tr, timeText, x, groupY + 4, 0xFFFFFFFF, true);
                }
            } else {
                // Digital time
                if (mode == GuiTimeConfig.DisplayMode.TIME_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    String timeText = String.format(Locale.ROOT, "%02d:%02d",
                            (int)(((dayTicks / 1000f) + 6f) % 24f),
                            (int)((((dayTicks / 1000f) + 6f) % 1f) * 60f)
                    );
                    ctx.drawText(tr, timeText, x, groupY + 4, 0xFFFFFFFF, true);
                    x += textW + gap;
                }
                // Sleep indicator
                if (wantExcl) {
                    if (warnSoon) {
                        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, EXCL_ICON2, x, groupY, 0, 0, exclW, 16, exclW, 16);
                    } else if (canSleep) {
                        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, EXCL_ICON, x, groupY, 0, 0, exclW, 16, exclW, 16);
                    }
                }
                x += exclW + gap;
                // Phantom indicator
                if (cfg.showPhantomIndicator && inOverworld && isNight && ticksSinceRest >= cfg.phantomThresholdTicks) {
                    ctx.drawTexture(RenderPipelines.GUI_TEXTURED, PHANTOM_ICON, x, groupY, 0, 0, phantomW, phantomW, phantomW, phantomW);
                    x += phantomW + gap;
                }
                // Clock icon - render the actual in-game clock item
                if (mode == GuiTimeConfig.DisplayMode.ICON_ONLY || mode == GuiTimeConfig.DisplayMode.BOTH) {
                    ctx.drawItem(clockStack, x, groupY);
                }
            }
        });
    }
}
