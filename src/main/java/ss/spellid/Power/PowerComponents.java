package ss.spellid.Power;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import ss.spellid.TheSpell;

public class PowerComponents {

    public static Identifier PLAYER_POWER_DATA = Identifier.fromNamespaceAndPath
            (TheSpell.MOD_ID, "player_power_data");

    public static void register(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            //attachDefaultData(player);
        });

    }
    /*private static void attachDefaultData(ServerPlayer player) {
        if (!player.has(PlayerPowerData.TYPE)) {
            player.setComponent(PlayerPowerData.TYPE, PlayerPowerData.EMPTY);
        }
    }*/
}