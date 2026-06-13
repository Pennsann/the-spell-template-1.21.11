package ss.spellid.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import ss.spellid.block.ModBlockEntities;
import ss.spellid.block.entity.NightmareSeedBlockEntity;
import ss.spellid.components.NightmareInstance;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.nightmare.Nightmare;
import ss.spellid.nightmare.NightmareManager;
import ss.spellid.party.Party;
import ss.spellid.party.PartyManager;

import java.util.Set;
import java.util.UUID;

public class NightmareSeedBlock extends BaseEntityBlock {
    public NightmareSeedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(NightmareSeedBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NightmareSeedBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        // Get block entity and nightmare ID
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof NightmareSeedBlockEntity seedBe)) return InteractionResult.PASS;

        String nightmareIdStr = seedBe.getNightmareId();
        if (nightmareIdStr == null) {
            serverPlayer.displayClientMessage(Component.literal("§cThis seed has no nightmare bound to it."), false);
            return InteractionResult.FAIL;
        }

        Identifier nightmareId = Identifier.parse(nightmareIdStr);
        Nightmare nightmare = NightmareManager.get(nightmareId);
        if (nightmare == null) {
            serverPlayer.displayClientMessage(Component.literal("§cUnknown nightmare type."), false);
            return InteractionResult.FAIL;
        }

        // Rank check
        var rankComp = RankComponentInitializer.RANK_KEY.get(serverPlayer);
        if (rankComp.getRank().ordinal() < nightmare.minRank().ordinal()) {
            serverPlayer.displayClientMessage(Component.literal(
                    "§cYou need to be at least " + nightmare.minRank().getDisplayName() + " to enter this nightmare."), false);
            return InteractionResult.FAIL;
        }

        // Party check
        Party party = PartyManager.getParty(serverPlayer);
        int partySize = party != null ? party.size() : 1;

        if (partySize < nightmare.minPlayers()) {
            serverPlayer.displayClientMessage(Component.literal(
                    "§cThis nightmare requires at least " + nightmare.minPlayers() + " player(s)."), false);
            return InteractionResult.FAIL;
        }

        if (partySize > nightmare.maxPlayers()) {
            serverPlayer.displayClientMessage(Component.literal(
                    "§cThis nightmare allows at most " + nightmare.maxPlayers() + " player(s)."), false);
            return InteractionResult.FAIL;
        }

        // Get nightmare dimension
        ResourceKey<Level> nightmareKey = nightmare.dimensionKey();
        ServerLevel nightmareLevel = serverPlayer.level().getServer().getLevel(nightmareKey);
        if (nightmareLevel == null) {
            serverPlayer.displayClientMessage(Component.literal("§cNightmare dimension not found!"), false);
            return InteractionResult.FAIL;
        }

        // Teleport leader (and party members if in a party)
        if (party != null) {
            for (UUID memberId : party.getMembers()) {
                ServerPlayer member = serverPlayer.level().getServer().getPlayerList().getPlayer(memberId);
                if (member == null) continue;

                // Rank check for each member
                var memberRank = RankComponentInitializer.RANK_KEY.get(member);
                if (memberRank.getRank().ordinal() < nightmare.minRank().ordinal()) {
                    serverPlayer.displayClientMessage(Component.literal(
                            "§c" + member.getName().getString() + " does not meet the rank requirement."), false);
                    return InteractionResult.FAIL;
                }

                teleportToNightmare(member, nightmareLevel, nightmareId, pos, level.dimension());
            }
        } else {
            teleportToNightmare(serverPlayer, nightmareLevel, nightmareId, pos, level.dimension());
        }

        serverPlayer.displayClientMessage(Component.literal("§5The nightmare consumes you..."), false);
        return InteractionResult.SUCCESS;
    }

    private void teleportToNightmare(ServerPlayer player, ServerLevel nightmareLevel,
                                     Identifier nightmareId, BlockPos seedPos,
                                     ResourceKey<Level> seedDimension) {
        // Store nightmare instance data on the player
        NightmareInstance instance = RankComponentInitializer.NIGHTMARE_INSTANCE.get(player);
        instance.setNightmareId(nightmareId);
        instance.setCompleted(false);
        instance.setSeedPos(seedPos);
        instance.setSeedDimension(seedDimension);

        // Teleport to nightmare spawn
        double x = nightmareLevel.getRespawnData().pos().getX() + 0.5;
        double y = nightmareLevel.getRespawnData().pos().getY();
        double z = nightmareLevel.getRespawnData().pos().getZ() + 0.5;

        // Place completion block near spawn
        BlockPos spawnPos = nightmareLevel.getRespawnData().pos();
        BlockPos completionPos = spawnPos.offset(2, 0, 0);
        nightmareLevel.setBlock(completionPos, net.minecraft.world.level.block.Blocks.GOLD_BLOCK.defaultBlockState(), 3);

        player.teleportTo(nightmareLevel, x, y, z, Set.of(),
                player.getYRot(), player.getXRot(), false);

        player.displayClientMessage(Component.literal(
                "§5You are pulled into the nightmare..."), false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}