package ss.spellid.aspect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class PotionEffectPower implements AspectPower {
    private final MobEffectInstance effect;

    public PotionEffectPower(MobEffectInstance effect) {
        this.effect = effect;
    }

    @Override
    public void apply(Player player) {
        player.addEffect(new MobEffectInstance(effect));
    }

    @Override
    public void remove(Player player) {
        player.removeEffect(effect.getEffect());
    }
}