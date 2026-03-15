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
import ss.spellid.TheSpell;
import ss.spellid.aspect.Aspects;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.effect.ModEffects;
import ss.spellid.ranks.Ranks;

import java.util.Set;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class NightmareCompletionHandler {
    private static final Identifier NIGHTMARE_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "first_nightmare");
    private static final ResourceKey<Level> NIGHTMARE_KEY = ResourceKey.create(Registries.DIMENSION, NIGHTMARE_ID);
    private static final BlockPos COMPLETION_POS_OFFSET = new BlockPos(0, 2, 0); // two blocks above spawn

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.level().dimension().equals(NIGHTMARE_KEY)) continue;
                var essence = ESSENCE.get(player);
                if (!essence.hasNightmareSeed()) continue;

                ServerLevel nightmareLevel = (ServerLevel) player.level();
                BlockPos spawn = nightmareLevel.getRespawnData().pos();
                BlockPos completionPos = spawn.offset(COMPLETION_POS_OFFSET);

                // Debug logging to see positions
                TheSpell.LOGGER.info("Player {} at pos {}, completion pos {}", player.getName().getString(), player.blockPosition(), completionPos);

                // Exact check: player stands on the gold block (block below is the gold block)
                if (player.blockPosition().below().equals(completionPos)) {
                    TheSpell.LOGGER.info("Exact completion condition met for {}", player.getName().getString());
                    completeNightmare(player);
                }
                // Optional range check (within 1.5 blocks) – uncomment if exact check fails
                /*
                else if (player.blockPosition().distSqr(completionPos.above()) < 2.25) {
                    TheSpell.LOGGER.info("Range completion condition met for {}", player.getName().getString());
                    completeNightmare(player);
                }
                */
            }
        });
    }

    private static void completeNightmare(ServerPlayer player) {
        TheSpell.LOGGER.info("Player {} completed the First Nightmare!", player.getName().getString());

        var essence = ESSENCE.get(player);
        essence.setNightmareSeed(false);
        player.removeEffect(ModEffects.NIGHTMARE_SEED);

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

        var rankComp = RANK_KEY.get(player);
        rankComp.setRank(Ranks.SLEEPER);

        // Grant a random starter aspect (full identifier)
        Identifier aspectId = Aspects.getRandomStarterId();
        essence.setAspectId(aspectId.toString());

        player.displayClientMessage(Component.literal("§aYou have conquered the First Nightmare! You are now a Sleeper with the " + Aspects.get(aspectId).getDisplayName().getString() + " aspect."), false);
    }
}