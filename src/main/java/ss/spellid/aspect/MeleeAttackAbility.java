package ss.spellid.aspect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface MeleeAttackAbility {
    void onMeleeHit(ServerPlayer player, LivingEntity target);
}