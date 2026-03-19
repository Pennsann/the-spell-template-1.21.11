package ss.spellid.aspect.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

public interface AspectAbility {
    Identifier getId();
    int getCooldownTicks();        // cooldown in ticks (20 ticks = 1 second)
    int getEssenceCost();           // essence cost per use
    boolean canUse(ServerPlayer player);  // optional extra checks (e.g., is in correct dimension)
    void use(ServerPlayer player);        // perform the ability
}