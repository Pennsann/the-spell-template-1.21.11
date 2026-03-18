package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import ss.spellid.components.RankComponentInitializer;

import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class EssenceRegenHandler {
    private static final int MIN_FOOD_LEVEL = 18; // need at least this much food to regen

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                var essence = ESSENCE.get(player);
                // Only regen if player has enough food and hasn't reached max essence
                if (player.getFoodData().getFoodLevel() >= MIN_FOOD_LEVEL && essence.getCurrentEssence() < essence.getMaxEssence()) {
                    essence.tickRegen();
                }
            }
        });
    }
}