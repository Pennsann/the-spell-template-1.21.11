package ss.spellid.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import ss.spellid.TheSpell;
import ss.spellid.components.RankComponent;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.ranks.Ranks;

import java.util.Set;

public class CitadelGatewayBlock extends Block {
    public CitadelGatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(serverPlayer);
            Ranks currentRank = rankComp.getRank();

            // If player is in the Dream Realm dimension
            Identifier dreamRealmId = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm");
            ResourceKey<Level> dreamRealmKey = ResourceKey.create(Registries.DIMENSION, dreamRealmId);

            if (level.dimension().equals(dreamRealmKey)) {
                // In Dream Realm
                if (currentRank == Ranks.SLEEPER) {
                    // Rank up to AWAKENED
                    rankComp.setRank(Ranks.AWAKENED);
                    serverPlayer.displayClientMessage(
                            Component.literal("§dThe Citadel's power flows through you... You are now Awakened!"),
                            false
                    );

                    // Set respawn point back to overworld spawn
                    ServerLevel overworld = serverPlayer.level().getServer().overworld();
                    BlockPos spawnPos = overworld.getRespawnData().pos();
                    ServerPlayer.RespawnConfig respawnConfig = new ServerPlayer.RespawnConfig(
                            LevelData.RespawnData.of(overworld.dimension(), spawnPos, serverPlayer.getYRot(), serverPlayer.getXRot()),
                            true
                    );
                    serverPlayer.setRespawnPosition(respawnConfig, true);

                    // Teleport to overworld spawn
                    serverPlayer.teleportTo(
                            overworld,
                            spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                            Set.of(),
                            serverPlayer.getYRot(),
                            serverPlayer.getXRot(),
                            false
                    );

                } else if (currentRank.ordinal() >= Ranks.AWAKENED.ordinal()) {
                    // Already Awakened or higher: just teleport back to overworld
                    ServerLevel overworld = serverPlayer.level().getServer().overworld();
                    BlockPos spawnPos = overworld.getRespawnData().pos();

                    serverPlayer.teleportTo(
                            overworld,
                            spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                            Set.of(),
                            serverPlayer.getYRot(),
                            serverPlayer.getXRot(),
                            false
                    );

                    serverPlayer.displayClientMessage(
                            Component.literal("§aYou return to the waking world."),
                            false
                    );
                } else {
                    // Should not happen (PLAYER rank can't be in Dream Realm normally)
                    serverPlayer.displayClientMessage(
                            Component.literal("§cYou are not yet ready to use this gateway."),
                            false
                    );
                }
            } else {
                // Not in Dream Realm – maybe just set spawn point (original behavior)
                ServerPlayer.RespawnConfig respawnConfig = new ServerPlayer.RespawnConfig(
                        LevelData.RespawnData.of(level.dimension(), pos, player.getYRot(), player.getXRot()),
                        true
                );
                serverPlayer.setRespawnPosition(respawnConfig, true);
                serverPlayer.displayClientMessage(
                        Component.literal("§7Your tether to this Citadel has been renewed."),
                        false
                );
            }
        }
        return InteractionResult.SUCCESS;
    }
}