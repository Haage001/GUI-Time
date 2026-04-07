package haage.gui_time;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class GuiTimeConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("GUI Time Settings"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Corner selector with readable names
        general.addEntry(
                entryBuilder.startEnumSelector(
                                Component.literal("HUD Corner"),
                                GuiTimeConfig.Corner.class,
                                GuiTimeConfig.get().corner
                        )
                        .setDefaultValue(GuiTimeConfig.Corner.BOTTOM_RIGHT)
                        .setEnumNameProvider(corner -> switch ((GuiTimeConfig.Corner)corner) {
                            case TOP_LEFT -> Component.literal("Top Left");
                            case TOP_RIGHT -> Component.literal("Top Right");
                            case TOP_CENTER -> Component.literal("Top Center");
                            case BOTTOM_LEFT -> Component.literal("Bottom Left");
                            case BOTTOM_RIGHT -> Component.literal("Bottom Right");
                        })
                        .setSaveConsumer(val -> {
                            GuiTimeConfig.get().corner = val;
                            GuiTimeConfig.save();
                        })
                        .build()
        );

        // Display mode selector with readable names
        general.addEntry(
                entryBuilder.startEnumSelector(
                                Component.literal("Display Mode"),
                                GuiTimeConfig.DisplayMode.class,
                                GuiTimeConfig.get().displayMode
                        )
                        .setDefaultValue(GuiTimeConfig.DisplayMode.BOTH)
                        .setEnumNameProvider(mode -> switch ((GuiTimeConfig.DisplayMode)mode) {
                            case NONE -> Component.literal("None");
                            case ICON_ONLY -> Component.literal("Icon Only");
                            case TIME_ONLY -> Component.literal("Time Only");
                            case BOTH -> Component.literal("Both");
                        })
                        .setSaveConsumer(val -> {
                            GuiTimeConfig.get().displayMode = val;
                            GuiTimeConfig.save();
                        })
                        .setTooltipSupplier(mode -> Optional.of(new Component[]{
                                switch ((GuiTimeConfig.DisplayMode)mode) {
                                    case NONE      -> Component.literal("Hide both clock and time; show only the sleep and phantom indicators if enabled.");
                                    case ICON_ONLY -> Component.literal("Only the analog clock icon will be shown.");
                                    case TIME_ONLY -> Component.literal("Only the digital clock will be shown.");
                                    case BOTH      -> Component.literal("Both the icon and the digital clock will be shown.");
                                }
                        }))
                        .build()
        );

        // Sleep indicator toggle
        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Show Sleep Indicator"),
                                GuiTimeConfig.get().showSleepIndicator
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("When on, a red '!' will appear if you can sleep."))
                        .setSaveConsumer(val -> {
                            GuiTimeConfig.get().showSleepIndicator = val;
                            GuiTimeConfig.save();
                        })
                        .build()
        );

        // Phantom indicator toggle
        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Show Phantom Indicator"),
                                GuiTimeConfig.get().showPhantomIndicator
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("When on, a phantom icon will appear once you've gone 72,000 ticks without sleeping and it's night."))
                        .setSaveConsumer(val -> {
                            GuiTimeConfig.get().showPhantomIndicator = val;
                            GuiTimeConfig.save();
                        })
                        .build()
        );

        return builder.build();
    }
}
