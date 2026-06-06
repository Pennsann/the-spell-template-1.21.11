package ss.spellid.util;

import net.minecraft.server.level.ServerPlayer;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.ranks.Ranks;

import java.util.Map;

public class ScalingHelper {
    public static double getScaledValue(ServerPlayer player, Map<Ranks, Double> rankToValue, double defaultValue) {
        Ranks rank = RankComponentInitializer.RANK_KEY.get(player).getRank();
        return rankToValue.getOrDefault(rank, defaultValue);
    }

    public static int getScaledInt(ServerPlayer player, Map<Ranks, Integer> rankToValue, int defaultValue) {
        Ranks rank = RankComponentInitializer.RANK_KEY.get(player).getRank();
        return rankToValue.getOrDefault(rank, defaultValue);
    }
}