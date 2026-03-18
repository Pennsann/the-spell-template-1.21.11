package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import ss.spellid.TheSpell;
import ss.spellid.components.RankComponent;
import ss.spellid.components.RankComponentInitializer;

public class HungerRegenHandler {
    private static final int INTERVAL = 1200; // 1 minute (20 ticks * 60)

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // Only apply once per minute
                if (player.tickCount % INTERVAL != 0) continue;

                RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(player);
                int regen = rankComp.getRank().getHungerRegenPerMinute();
                if (regen <= 0) continue;

                FoodData food = player.getFoodData();
                int currentFood = food.getFoodLevel();
                if (currentFood >= 20) continue; // already full

                // Add hunger, cap at 20
                int newFood = Math.min(currentFood + regen, 20);
                food.setFoodLevel(newFood);

                // Optionally add some saturation too (scaled by rank)
                // You could also add a separate method for saturation regen
                // For simplicity, we'll add a small amount of saturation as well
                float currentSaturation = food.getSaturationLevel();
                float maxSaturation = newFood; // saturation cannot exceed food level
                if (currentSaturation < maxSaturation) {
                    float saturationRegen = regen * 0.5f; // half a shank per hunger point
                    food.setSaturation(Math.min(currentSaturation + saturationRegen, maxSaturation));
                }
            }
        });
    }
}