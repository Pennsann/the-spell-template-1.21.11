package ss.spellid.aspect.ability;

import net.minecraft.server.level.ServerPlayer;

public interface ChanneledAbility extends AspectAbility {
    /** How often (in ticks) the ability should tick (e.g., 5 for 4x per second). */
    int getTickInterval();

    /** Essence cost per tick. */
    int getEssenceCostPerTick();

    /** Called once when channeling starts. */
    default void onStart(ServerPlayer player) {}

    /** Called each tick while channeling (use for damage, block placement, etc.). */
    void onTick(ServerPlayer player);

    /** Called when channeling stops (manually, cooldown, or out of essence). */
    default void onStop(ServerPlayer player) {}
}