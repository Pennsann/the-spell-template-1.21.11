package ss.spellid.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class FrozenEffect extends MobEffect {
    public FrozenEffect() {
        super(MobEffectCategory.HARMFUL, 0x9BD4FF);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // Apply very strong slowness to freeze the target
        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 255, false, false, true));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Tick every tick to keep the slowness active
        return true;
    }
}