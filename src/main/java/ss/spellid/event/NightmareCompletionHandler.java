package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import ss.spellid.TheSpell;
import ss.spellid.aspect.Aspects;
import ss.spellid.block.custom.NightmareSeedBlock;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.components.NightmareInstance;
import ss.spellid.effect.ModEffects;
import ss.spellid.nightmare.Nightmare;
import ss.spellid.nightmare.NightmareManager;
import ss.spellid.ranks.Ranks;

import java.util.Set;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class NightmareCompletionHandler {
    private static final BlockPos COMPLETION_POS_OFFSET = new BlockPos(2, 0, 0); // two blocks east of spawn

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                var instance = RankComponentInitializer.NIGHTMARE_INSTANCE.get(player);
                Identifier nightmareId = instance.getNightmareId();
                if (nightmareId == null) continue;

                Nightmare nightmare = NightmareManager.get(nightmareId);
                if (nightmare == null) continue;

                if (!player.level().dimension().equals(nightmare.dimensionKey())) continue;

                var essence = ESSENCE.get(player);
                if (nightmare.entryType() == Nightmare.EntryType.SLEEP && !essence.hasNightmareSeed()) continue;

                ServerLevel nightmareLevel = (ServerLevel) player.level();
                BlockPos spawn = nightmareLevel.getRespawnData().pos();
                BlockPos completionPos = spawn.offset(COMPLETION_POS_OFFSET);

                // Check if player is within 1.5 blocks of the completion block
                double distance = player.blockPosition().distSqr(completionPos);
                if (distance < 2.25) {
                    completeNightmare(player, nightmare, instance);
                }
            }
        });
    }

    private static void completeNightmare(ServerPlayer player, Nightmare nightmare, NightmareInstance instance) {
        if (instance.isCompleted()) return;

        Identifier nightmareId = instance.getNightmareId();
        if (nightmareId == null) return;

        // Mark global completion
        NightmareManager.complete(nightmareId, player);

        // Remove seed and effect (if any)
        var essence = ESSENCE.get(player);
        essence.setNightmareSeed(false);
        player.removeEffect(ModEffects.NIGHTMARE_SEED);

        // Grant aspect if present
        if (nightmare.aspectId() != null) {
            essence.setAspectId(nightmare.aspectId().toString());
        }

        // Rank up if applicable
        if (nightmare.rankUpTo() != null) {
            var rankComp = RANK_KEY.get(player);
            rankComp.setRank(nightmare.rankUpTo());
            player.displayClientMessage(Component.literal("§dYou have advanced to " + nightmare.rankUpTo().getDisplayName() + "!"), false);
        }

        // Remove the seed block from the Dream Realm (if stored)
        BlockPos seedPos = instance.getSeedPos();
        ResourceKey<Level> seedDim = instance.getSeedDimension();
        if (seedPos != null && seedDim != null) {
            ServerLevel seedWorld = player.level().getServer().getLevel(seedDim);
            if (seedWorld != null && seedWorld.getBlockState(seedPos).getBlock() instanceof NightmareSeedBlock) {
                seedWorld.setBlock(seedPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                TheSpell.LOGGER.info("Removed nightmare seed at {} in {}", seedPos, seedDim.identifier());
            }
        }

        // Teleport back to overworld spawn (or player's spawn? we'll keep overworld spawn for now)
        ServerLevel overworld = player.level().getServer().overworld();
        BlockPos spawn = overworld.getRespawnData().pos();
        player.teleportTo(overworld, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), player.getYRot(), player.getXRot(), false);

        // Clear nightmare instance data
        instance.setNightmareId(null);
        instance.setCompleted(true);
        instance.setSeedPos(null);
        instance.setSeedDimension(null);

        // Message
        String msg = nightmare.aspectId() != null ?
                "§aYou have conquered the Nightmare! You are now a " + (nightmare.rankUpTo() != null ? nightmare.rankUpTo().getDisplayName() : "") + " with the " + nightmare.displayName() + " aspect." :
                "§aYou have conquered the Nightmare! You are now a " + (nightmare.rankUpTo() != null ? nightmare.rankUpTo().getDisplayName() : "");
        player.displayClientMessage(Component.literal(msg), false);
    }
}