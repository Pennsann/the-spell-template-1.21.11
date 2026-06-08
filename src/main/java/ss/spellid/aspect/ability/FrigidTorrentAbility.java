package ss.spellid.aspect.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ss.spellid.TheSpell;
import ss.spellid.util.FrostFlawHelper;
import ss.spellid.block.ModBlocks;
import ss.spellid.block.custom.IceSheetBlock;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.effect.ModEffects;

import java.util.List;

public class FrigidTorrentAbility implements ChanneledAbility {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "frigid_torrent");
    private static final int COOLDOWN_TICKS = 100;
    private static final int TICK_INTERVAL = 5;
    private static final int ESSENCE_COST_PER_TICK = 2;
    private static final int RANGE = 5;
    private static final double ANGLE_RADIANS = Math.toRadians(45.0);
    private static final float DAMAGE = 3.0f;
    private static final int FLAW_REAPPLY_INTERVAL = 40;

    @Override
    public Identifier getId() { return ID; }

    @Override
    public int getCooldownTicks() { return COOLDOWN_TICKS; }

    @Override
    public int getEssenceCost() { return 0; }

    @Override
    public boolean canUse(ServerPlayer player) { return true; }

    @Override
    public void use(ServerPlayer player) {}

    @Override
    public int getTickInterval() { return TICK_INTERVAL; }

    @Override
    public int getEssenceCostPerTick() { return ESSENCE_COST_PER_TICK; }

    @Override
    public void onStart(ServerPlayer player) {
        TheSpell.LOGGER.info("Frigid Torrent started by {}", player.getName().getString());
        FrostFlawHelper.applyFrostFlaw(player, 2); // Awakened flaw
        EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
        essence.setLastChannelFlawTick(player.level().getGameTime());
    }

    @Override
    public void onTick(ServerPlayer player) {
        applyConeEffects(player);
        spawnConeParticles(player);
        freezeGroundInCone(player);
        EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
        long now = player.level().getGameTime();
        long lastFlaw = essence.getLastChannelFlawTick();
        if (now - lastFlaw >= FLAW_REAPPLY_INTERVAL) {
            FrostFlawHelper.applyFrostFlaw(player, 2);
            essence.setLastChannelFlawTick(now);
        }
    }

    @Override
    public void onStop(ServerPlayer player) {
        TheSpell.LOGGER.info("Frigid Torrent stopped for {}", player.getName().getString());
    }

    private void applyConeEffects(ServerPlayer player) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double cosThreshold = Math.cos(ANGLE_RADIANS);

        AABB searchBox = AABB.ofSize(origin, RANGE * 2, RANGE * 2, RANGE * 2);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive());
        for (LivingEntity target : targets) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
            double dist = toTarget.length();
            if (dist > RANGE) continue;
            Vec3 dir = toTarget.normalize();
            if (look.dot(dir) < cosThreshold) continue;

            target.hurt(player.damageSources().indirectMagic(player, player), DAMAGE);
            Vec3 knockback = dir.scale(0.2);
            target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.1, knockback.z));

            var chilledHolder = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.CHILLED);
            var frozenHolder = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.FROZEN);
            if (target.hasEffect(chilledHolder)) {
                target.addEffect(new MobEffectInstance(frozenHolder, 20, 0));
            } else {
                target.addEffect(new MobEffectInstance(chilledHolder, 40, 0));
            }
        }
    }

    private void spawnConeParticles(ServerPlayer player) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double cosThreshold = Math.cos(ANGLE_RADIANS);
        int particleCount = 80;

        for (int i = 0; i < particleCount; i++) {
            Vec3 randomDir = new Vec3(
                    look.x + (player.getRandom().nextDouble() - 0.5) * 1.8,
                    look.y + (player.getRandom().nextDouble() - 0.5) * 1.8,
                    look.z + (player.getRandom().nextDouble() - 0.5) * 1.8
            ).normalize();
            if (look.dot(randomDir) < cosThreshold) continue;
            double dist = player.getRandom().nextDouble() * RANGE;
            Vec3 pos = origin.add(randomDir.scale(dist));
            player.level().addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, 0, 0);
        }

        if (player.getRandom().nextInt(10) == 0) {
            player.level().addParticle(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(), 0, 0, 0);
        }
    }

    private void freezeGroundInCone(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        double cosThreshold = Math.cos(ANGLE_RADIANS);
        int radius = RANGE;
        Level level = player.level();

        int blocksChanged = 0;
        for (int dx = -radius; dx <= radius && blocksChanged < 30; dx++) {
            for (int dz = -radius; dz <= radius && blocksChanged < 30; dz++) {
                double dist2D = Math.sqrt(dx*dx + dz*dz);
                if (dist2D > radius) continue;
                Vec3 direction = new Vec3(dx, 0, dz).normalize();
                Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();
                if (horizontalLook.dot(direction) < cosThreshold) continue;

                BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE,
                        player.blockPosition().offset(dx, 0, dz));
                BlockPos blockBelow = groundPos.below();
                var state = level.getBlockState(blockBelow);

                if (state.getBlock() == Blocks.WATER) {
                    level.setBlock(blockBelow, Blocks.FROSTED_ICE.defaultBlockState(), 3);
                    blocksChanged++;
                } else if (state.isSolid() && !(state.getBlock() instanceof IceSheetBlock)) {
                    BlockPos onTop = blockBelow.above();
                    if (level.getBlockState(onTop).canBeReplaced()) {
                        level.setBlock(onTop, ModBlocks.ICE_SHEET.defaultBlockState(), 3);
                        blocksChanged++;
                    }
                }
            }
        }
    }
}