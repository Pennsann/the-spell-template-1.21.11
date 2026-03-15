package ss.spellid.aspect;

import net.minecraft.world.entity.player.Player;

public interface AspectPower {
    void apply(Player player);
    void remove(Player player);
}