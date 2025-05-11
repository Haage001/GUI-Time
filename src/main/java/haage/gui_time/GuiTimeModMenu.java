package haage.gui_time;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class GuiTimeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        // Delegate to your Cloth Config screen
        return parent -> GuiTimeConfigScreen.create(parent);
    }
}
