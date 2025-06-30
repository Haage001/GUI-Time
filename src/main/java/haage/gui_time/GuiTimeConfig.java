package haage.gui_time;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds the user’s HUD-corner choice, DisplayMode, sleep/phantom-indicator toggles,
 * reads/writes them to config/gui-time.json in the Fabric config folder.
 */
public class GuiTimeConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("gui-time.json");
    private static GuiTimeConfig instance;

    /** All four corners for the HUD */
    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        TOP_CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    /** What to show in the HUD */
    public enum DisplayMode {
        NONE,       // ← nothing but sleep/phantom indicators if enabled
        ICON_ONLY,
        TIME_ONLY,
        BOTH
    }

    /** The HUD corner choice, written/read as JSON */
    public Corner corner = Corner.BOTTOM_RIGHT;

    /** What to show: icon, time, both, or none (for indicators-only) */
    public DisplayMode displayMode = DisplayMode.BOTH;

    /** Show a red “!” when the player can sleep */
    public boolean showSleepIndicator = true;

    /** Show a phantom icon once 72 000 ticks have passed since last sleep */
    public boolean showPhantomIndicator = true;

    /** Number of ticks without sleeping before debug test triggers the phantom icon */
    public int phantomThresholdTicks = 72000;

    /** Accessor; loads from disk on first call. */
    public static GuiTimeConfig get() {
        if (instance == null) load();
        return instance;
    }

    /** Load from JSON or create a default if missing/corrupt. */
    public static void load() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                    instance = gson.fromJson(r, GuiTimeConfig.class);
                }
            } else {
                instance = new GuiTimeConfig();
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
            instance = new GuiTimeConfig();
        }
    }

    /** Save current instance back to disk. */
    public static void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                gson.toJson(get(), w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
