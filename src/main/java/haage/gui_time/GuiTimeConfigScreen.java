package haage.gui_time;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Optional;

public class GuiTimeConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("GUI Time Settings"));

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Corner selector with readable names
        general.addEntry(
                entryBuilder.startEnumSelector(
                                Text.literal("HUD Corner"),
                                GuiTimeConfig.Corner.class,
                                GuiTimeConfig.get().corner
                        )
                        .setDefaultValue(GuiTimeConfig.Corner.BOTTOM_RIGHT)
                        .setEnumNameProvider(corner -> switch ((GuiTimeConfig.Corner)corner) {
                            case TOP_LEFT -> Text.literal("Top Left");
                            case TOP_RIGHT -> Text.literal("Top Right");
                            case BOTTOM_LEFT -> Text.literal("Bottom Left");
                            case BOTTOM_RIGHT -> Text.literal("Bottom Right");
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
                                Text.literal("Display Mode"),
                                GuiTimeConfig.DisplayMode.class,
                                GuiTimeConfig.get().displayMode
                        )
                        .setDefaultValue(GuiTimeConfig.DisplayMode.BOTH)
                        .setEnumNameProvider(mode -> switch ((GuiTimeConfig.DisplayMode)mode) {
                            case NONE -> Text.literal("None");
                            case ICON_ONLY -> Text.literal("Icon Only");
                            case TIME_ONLY -> Text.literal("Time Only");
                            case BOTH -> Text.literal("Both");
                        })
                        .setSaveConsumer(val -> {
                            GuiTimeConfig.get().displayMode = val;
                            GuiTimeConfig.save();
                        })
                        .setTooltipSupplier(mode -> Optional.of(new Text[]{
                                switch ((GuiTimeConfig.DisplayMode)mode) {
                                    case NONE      -> Text.literal("Hide both clock and time; show only the sleep indicator if enabled.");
                                    case ICON_ONLY -> Text.literal("Only the analog clock icon will be shown.");
                                    case TIME_ONLY -> Text.literal("Only the digital clock will be shown.");
                                    case BOTH      -> Text.literal("Both the icon and the digital clock will be shown.");
                                }
                        }))
                        .build()
        );

        // Sleep indicator toggle
        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Text.literal("Show Sleep Indicator"),
                                GuiTimeConfig.get().showSleepIndicator
                        )
                        .setDefaultValue(true)
                        .setTooltip(Text.literal("When on, a red “!” will appear if you can sleep."))
                        .setSaveConsumer(val -> {
                            GuiTimeConfig.get().showSleepIndicator = val;
                            GuiTimeConfig.save();
                        })
                        .build()
        );

        return builder.build();
    }
}
