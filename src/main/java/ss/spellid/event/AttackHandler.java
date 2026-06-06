package ss.spellid.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import ss.spellid.aspect.MeleeAttackAbility;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.RankComponentInitializer;

public class AttackHandler {
    public static void register() {
        AttackEntityCallback.EVENT.register((Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) -> {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && entity instanceof LivingEntity target) {
                EssenceComponent essence = RankComponentInitializer.ESSENCE.get(serverPlayer);
                if (essence.hasPendingMeleeAbility()) {
                    MeleeAttackAbility ability = essence.getPendingMeleeAbility();
                    ability.onMeleeHit(serverPlayer, target);
                    essence.clearPendingMeleeAbility();
                }
            }
            return InteractionResult.PASS;
        });
    }
}