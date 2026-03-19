package ss.spellid.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import ss.spellid.TheSpell;
import ss.spellid.components.EssenceComponent;
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
            ResourceKey<Level> dreamRealmKey = ResourceKey.create(
                    Registries.DIMENSION,
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm")
            );
            if (!level.dimension().equals(dreamRealmKey)) {
                serverPlayer.displayClientMessage(Component.literal("§cThis gateway only works in the Dream Realm!"), false);
                return InteractionResult.FAIL;
            }

            RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(serverPlayer);
            Ranks currentRank = rankComp.getRank();

            if (currentRank == Ranks.SLEEPER) {
                rankComp.setRank(Ranks.AWAKENED);

                EssenceComponent essence = RankComponentInitializer.ESSENCE.get(serverPlayer);
                essence.setSaturationProgress(0);

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

                // Do NOT change respawn point – keep it at the Dream Realm citadel

                serverPlayer.displayClientMessage(
                        Component.literal("§dYou have Awakened! The power of the Dream Realm now flows through you."),
                        false
                );
                serverPlayer.displayClientMessage(
                        Component.literal("§bYou feel stronger, faster, and more resilient."),
                        true
                );
                TheSpell.LOGGER.info("Player {} awakened in Dream Realm", serverPlayer.getName().getString());

            } else {
                serverPlayer.displayClientMessage(
                        Component.literal("§7The gateway hums but does not respond."),
                        false
                );
            }
        }
        return InteractionResult.SUCCESS;
    }
}