package ss.spellid.aspect.ability.frost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ss.spellid.aspect.TickableAspectPower;
import ss.spellid.ranks.Ranks;
import ss.spellid.util.ScalingHelper;

import java.util.List;
import java.util.Map;

public class PermafrostAuraPower implements TickableAspectPower {
    private static final Map<Ranks, Double> RADIUS_BY_RANK = Map.of(
            Ranks.SLEEPER, 3.0,
            Ranks.AWAKENED, 4.0,
            Ranks.ASCENDED, 5.0
    );
    private static final Map<Ranks, Integer> SLOWNESS_AMP_BY_RANK = Map.of(
            Ranks.SLEEPER, 0,
            Ranks.AWAKENED, 1,
            Ranks.ASCENDED, 2
    );

    @Override
    public void apply(Player player) {
        // Nothing to do on apply; the tick method will handle the aura.
    }

    @Override
    public void remove(Player player) {
        // Nothing to do on remove; the aura stops because tick is no longer called
        // (the aspect is removed, so this power is removed from the list).
    }

    @Override
    public void tick(ServerPlayer player) {
        double radius = ScalingHelper.getScaledValue(player, RADIUS_BY_RANK, 3.0);
        int amplifier = ScalingHelper.getScaledInt(player, SLOWNESS_AMP_BY_RANK, 0);
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !(e instanceof ServerPlayer));
        for (LivingEntity target : entities) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, amplifier, false, false, true));
        }
    }
}