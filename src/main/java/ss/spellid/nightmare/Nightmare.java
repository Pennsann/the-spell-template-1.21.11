package ss.spellid.nightmare;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Nightmare(
        Identifier id,
        String displayName,
        EntryType entryType,
        int minPlayers,
        int maxPlayers,
        ResourceKey<Level> dimensionKey,
        Identifier aspectId // New field: which aspect to grant
) {
    public enum EntryType {
        SLEEP,   // For First Nightmare (solo, entered by sleeping)
        SEED     // For Second+ Nightmares (cohort, entered via seed block)
    }
}