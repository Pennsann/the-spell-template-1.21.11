package ss.spellid.aspect.ability.frost;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import ss.spellid.TheSpell;
import ss.spellid.aspect.ability.AspectAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import ss.spellid.util.FrostFlawHelper;
import ss.spellid.aspect.MeleeAttackAbility;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.effect.ModEffects;
import ss.spellid.ranks.Ranks;
import ss.spellid.util.ScalingHelper;

import java.util.Map;

public class PermafrostTouchAbility implements AspectAbility, MeleeAttackAbility {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "permafrost_touch");
    private static final int COOLDOWN_TICKS = 40;
    private static final int ESSENCE_COST = 5;

    private static final Map<Ranks, Integer> DAMAGE_BY_RANK = Map.of(
            Ranks.SLEEPER, 4,
            Ranks.AWAKENED, 6,
            Ranks.ASCENDED, 8
    );
    private static final Map<Ranks, Integer> SLOW_DURATION_BY_RANK = Map.of(
            Ranks.SLEEPER, 40,
            Ranks.AWAKENED, 60,
            Ranks.ASCENDED, 80
    );

    @Override
    public Identifier getId() { return ID; }

    @Override
    public int getCooldownTicks() { return COOLDOWN_TICKS; }

    @Override
    public int getEssenceCost() { return ESSENCE_COST; }

    @Override
    public boolean canUse(ServerPlayer player) { return true; }

    @Override
    public void use(ServerPlayer player) {
        FrostFlawHelper.applyFrostFlaw(player, 1); // Dormant flaw
        EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
        essence.setPendingMeleeAbility(this);
        player.displayClientMessage(Component.literal("§bYour touch turns to frost."), true);
    }

    @Override
    public void onMeleeHit(ServerPlayer player, LivingEntity target) {
        int bonusDamage = ScalingHelper.getScaledInt(player, DAMAGE_BY_RANK, 4);
        int slowDuration = ScalingHelper.getScaledInt(player, SLOW_DURATION_BY_RANK, 40);
        target.hurt(player.damageSources().playerAttack(player), bonusDamage);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, slowDuration, 2));

        var chilledHolder = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.CHILLED);
        var frozenHolder = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.FROZEN);
        if (target.hasEffect(chilledHolder)) {
            target.addEffect(new MobEffectInstance(frozenHolder, 20, 0));
            // Freeze sound — louder crack when upgrading to frozen
            player.level().playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.PLAYER_HURT_FREEZE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f,
                    0.8f  // lower pitch = deeper freeze crack
            );
            player.level().playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.6f,
                    1.6f  // high pitch = icy shatter
            );
        } else {
            target.addEffect(new MobEffectInstance(chilledHolder, 100, 0));
            // Chill sound — softer initial frost
            player.level().playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.POWDER_SNOW_STEP,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.8f,
                    1.4f
            );
            player.level().playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.PLAYER_HURT_FREEZE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5f,
                    1.2f
            );
        }
    }
}