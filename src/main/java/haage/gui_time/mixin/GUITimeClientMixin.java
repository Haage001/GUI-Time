package haage.gui_time.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import haage.gui_time.GUITimeClient;

@Mixin(ClientPacketListener.class)
public class GUITimeClientMixin {
    @Inject(method = "handleAwardStats", at = @At("TAIL"))
    private void onHandleAwardStats(ClientboundAwardStatsPacket packet, CallbackInfo ci) {
        Stat<?> timeSinceRest = Stats.CUSTOM.get(Stats.TIME_SINCE_REST);
        if (timeSinceRest != null && packet.stats().containsKey(timeSinceRest)) {
            GUITimeClient.setTicksSinceRest(packet.stats().getInt(timeSinceRest));
        }
    }
}