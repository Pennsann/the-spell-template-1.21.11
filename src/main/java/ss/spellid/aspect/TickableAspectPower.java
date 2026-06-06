package ss.spellid.aspect;

import net.minecraft.server.level.ServerPlayer;

/**
 * An AspectPower that needs to be ticked periodically (e.g., auras).
 * The tick method will be called approximately once per second on the server.
 */
public interface TickableAspectPower extends AspectPower {
    void tick(ServerPlayer player);
}