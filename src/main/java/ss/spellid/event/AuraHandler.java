package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.AspectPower;
import ss.spellid.aspect.TickableAspectPower;
import ss.spellid.aspect.Aspects;
import ss.spellid.components.RankComponentInitializer;

public class AuraHandler {
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Tick every 20 ticks (1 second)
            if (++tickCounter < 20) return;
            tickCounter = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // Get the player's aspect
                var essence = RankComponentInitializer.ESSENCE.get(player);
                String aspectId = essence.getAspectId();
                if (aspectId == null) continue;
                Aspect aspect = Aspects.get(net.minecraft.resources.Identifier.parse(aspectId));
                if (aspect == null) continue;

                // Loop through all powers of the aspect
                for (AspectPower power : aspect.getPowers()) {
                    if (power instanceof TickableAspectPower tickable) {
                        tickable.tick(player);
                    }
                }
            }
        });
    }
}