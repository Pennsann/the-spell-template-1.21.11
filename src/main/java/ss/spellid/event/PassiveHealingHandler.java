package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import ss.spellid.TheSpell;
import ss.spellid.components.RankComponent;
import ss.spellid.components.RankComponentInitializer;

public class PassiveHealingHandler {
    private static final int REGEN_INTERVAL = 80; // 4 seconds
    private static final int COMBAT_COOLDOWN = 100; // 5 seconds
    private static final float EXHAUSTION_PER_HALF_HEART = 1.0f;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // Skip if dead, full health, or hungry
                if (player.isDeadOrDying() || player.getHealth() >= player.getMaxHealth()) continue;
                if (player.getFoodData().getFoodLevel() < 18) continue;

                // Combat check: if hurt by mob or player in last 5 seconds, skip
                if (player.getLastHurtByMobTimestamp() < COMBAT_COOLDOWN || player.getLastHurtByMobTimestamp() < COMBAT_COOLDOWN) {
                    continue;
                }

                // Heal every 4 seconds
                if (player.tickCount % REGEN_INTERVAL == 0) {
                    RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(player);
                    int healAmount = rankComp.getRank().getHealPerRegenCycle();
                    if (healAmount <= 0) continue;

                    float newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
                    player.setHealth(newHealth);

                    int halfHearts = healAmount * 2;
                    float exhaustion = halfHearts * EXHAUSTION_PER_HALF_HEART;
                    player.causeFoodExhaustion(exhaustion);
                }
            }
        });
    }
}