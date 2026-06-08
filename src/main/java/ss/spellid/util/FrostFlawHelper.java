package ss.spellid.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import ss.spellid.components.FlawComponent;
import ss.spellid.components.RankComponentInitializer;

public class FrostFlawHelper {
    private static final String LEVEL_KEY = "frost_level";
    private static final String EXPIRY_KEY = "frost_expiry";

    public static void applyFrostFlaw(ServerPlayer player, int abilityRank) {
        FlawComponent flaw = RankComponentInitializer.FLAW.get(player);
        long now = player.level().getGameTime();
        long expiry = flaw.getLong(EXPIRY_KEY, 0L);
        int currentLevel = flaw.getInt(LEVEL_KEY, 0);

        // Base duration: 60 ticks (3 sec) for Dormant, 80 (4 sec) for Awakened, 100 (5 sec) for Ascended
        int duration;
        if (abilityRank == 1) duration = 60;
        else if (abilityRank == 2) duration = 120;
        else duration = 160;

        int newLevel;
        if (now < expiry) {
            newLevel = Math.min(currentLevel + 1, 3);
            flaw.setLong(EXPIRY_KEY, now + duration);
        } else {
            newLevel = Math.min(abilityRank, 3);
            flaw.setLong(EXPIRY_KEY, now + duration);
        }
        flaw.setInt(LEVEL_KEY, newLevel);

        int slownessLevel = newLevel - 1;
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, slownessLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, slownessLevel, false, false, true));
    }
}