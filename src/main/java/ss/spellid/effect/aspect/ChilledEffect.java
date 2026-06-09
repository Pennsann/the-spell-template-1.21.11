package ss.spellid.effect.aspect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ChilledEffect extends MobEffect {
    public ChilledEffect() {
        super(MobEffectCategory.HARMFUL, 0x6B8EFF);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // This effect does nothing each tick; it's just a marker.
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Don't tick every tick; we only need the effect to exist.
        return false;
    }
}