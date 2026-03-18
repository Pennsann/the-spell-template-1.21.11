package ss.spellid.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

public class CitadelGatewayBlock extends Block {
    public CitadelGatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        // Client-side early return
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(serverPlayer);
            Ranks currentRank = rankComp.getRank();

            // Set spawn point in the Dream Realm
            ServerPlayer.RespawnConfig respawnConfig = new ServerPlayer.RespawnConfig(
                    LevelData.RespawnData.of(level.dimension(), pos, player.getYRot(), player.getXRot()),
                    true
            );
            serverPlayer.setRespawnPosition(respawnConfig, true);

            if (currentRank == Ranks.SLEEPER) {
                // Rank up to AWAKENED
                rankComp.setRank(Ranks.AWAKENED);
                serverPlayer.displayClientMessage(
                        Component.literal("§dThe Citadel's power flows through you... You are now Awakened!"),
                        false
                );

                // Grant a temporary ability (speed boost)
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 1));
                serverPlayer.displayClientMessage(
                        Component.literal("§bYou feel faster!"),
                        true
                );

            } else {
                // For any other rank, just confirm the tether was set
                serverPlayer.displayClientMessage(
                        Component.literal("§7Your tether to this Citadel has been renewed."),
                        false
                );
            }
        }
        return InteractionResult.SUCCESS;
    }
}