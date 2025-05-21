package haage.gui_time.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import haage.gui_time.GUITimeClient;

import java.util.Map;

@Mixin(ClientPlayNetworkHandler.class)
public class GUITimeClientMixin {
    @Inject(method = "onStatistics", at = @At("TAIL"))
    private void onStatistics(StatisticsS2CPacket packet, CallbackInfo ci) {
        for (Map.Entry<Stat<?>, Integer> entry : packet.stats().entrySet()) {
            if (entry.getKey() == Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)) {
                GUITimeClient.setTicksSinceRest(entry.getValue());
                break;
            }
        }
    }
}