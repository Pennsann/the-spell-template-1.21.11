package ss.spellid.nightmare;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import ss.spellid.ranks.Ranks;

public record Nightmare(
        Identifier id,
        String displayName,
        EntryType entryType,
        int minPlayers,
        int maxPlayers,
        ResourceKey<Level> dimensionKey,
        Identifier aspectId,
        Ranks minRank,
        Ranks maxRank,
        Ranks rankUpTo,
        double rewardBonus
) {
    public enum EntryType {
        SLEEP,
        SEED
    }
}