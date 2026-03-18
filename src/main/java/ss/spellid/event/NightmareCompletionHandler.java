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
                if (!essence.hasNightmareSeed()) continue;

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
        if (instance.isCompleted()) {
            return;
        }

        Identifier nightmareId = instance.getNightmareId();
        if (nightmareId == null) {
            return;
        }

        // Mark global completion
        NightmareManager.complete(nightmareId, player);

        // Remove seed and effect
        var essence = ESSENCE.get(player);
        essence.setNightmareSeed(false);
        player.removeEffect(ModEffects.NIGHTMARE_SEED);

        // Grant the aspect associated with this nightmare
        essence.setAspectId(nightmare.aspectId().toString());

        // Teleport back to overworld spawn
        ServerLevel overworld = player.level().getServer().overworld();
        BlockPos spawn = overworld.getRespawnData().pos();
        player.teleportTo(
                overworld,
                spawn.getX(), spawn.getY(), spawn.getZ(),
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                false
        );

        // Promote to SLEEPER
        var rankComp = RANK_KEY.get(player);
        rankComp.setRank(Ranks.SLEEPER);

        // Clear nightmare instance data
        instance.setNightmareId(null);
        instance.setCompleted(true);

        player.displayClientMessage(Component.literal("§aYou have conquered the First Nightmare! You are now a Sleeper with the " + nightmare.displayName() + " aspect."), false);
    }
}