package ss.spellid.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class NightmareSeedEffect extends MobEffect {
    public NightmareSeedEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B0082); // Indigo color
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide() && entity.tickCount % 20 == 0) {
            // Optional: spawn particles later
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}