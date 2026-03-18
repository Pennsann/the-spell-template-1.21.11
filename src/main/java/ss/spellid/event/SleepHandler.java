package ss.spellid.event;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
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
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.components.NightmareInstance;
import ss.spellid.effect.ModEffects;
import ss.spellid.nightmare.Nightmare;
import ss.spellid.nightmare.NightmareManager;
import ss.spellid.ranks.Ranks;

import java.util.Set;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class SleepHandler {
    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayer player) {
                var essence = ESSENCE.get(player);
                boolean hasSeed = essence.hasNightmareSeed();

                // 2% chance to get nightmare seed (only if they don't have it AND rank is PLAYER)
                if (!hasSeed && player.getRandom().nextFloat() < 0.02f) {
                    var rankComp = RANK_KEY.get(player);
                    if (rankComp.getRank() == Ranks.PLAYER) {
                        essence.setNightmareSeed(true);
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                ModEffects.NIGHTMARE_SEED,
                                -1,
                                0,
                                false,
                                true,
                                true
                        ));
                        player.displayClientMessage(Component.literal("§5You feel a strange seed taking root in your soul..."), false);
                    }
                }

                // If they have the seed and are PLAYER, trigger First Nightmare
                if (hasSeed) {
                    var rankComp = RANK_KEY.get(player);
                    Ranks currentRank = rankComp.getRank();
                    if (currentRank == Ranks.PLAYER) {
                        // Get a random uncompleted solo nightmare
                        Identifier nightmareId = NightmareManager.getRandomUncompletedSolo(player);
                        if (nightmareId == null) {
                            player.displayClientMessage(Component.literal("§cNo nightmares remain..."), false);
                            return;
                        }
                        Nightmare nightmare = NightmareManager.get(nightmareId);
                        if (nightmare == null) return;
                        ResourceKey<Level> nightmareKey = nightmare.dimensionKey();

                        // Attach nightmare instance
                        var instance = RankComponentInitializer.NIGHTMARE_INSTANCE.get(player);
                        instance.setNightmareId(nightmareId);
                        instance.setCompleted(false);

                        ServerLevel nightmareLevel = player.level().getServer().getLevel(nightmareKey);
                        if (nightmareLevel != null) {
                            double x = nightmareLevel.getRespawnData().pos().getX();
                            double y = nightmareLevel.getRespawnData().pos().getY();
                            double z = nightmareLevel.getRespawnData().pos().getZ();

                            player.teleportTo(
                                    nightmareLevel,
                                    x, y, z,
                                    Set.of(),
                                    player.getYRot(),
                                    player.getXRot(),
                                    false
                            );
                            player.displayClientMessage(Component.literal("§5You awaken in a strange, empty realm... the First Nightmare begins!"), false);

                            // Place a gold block at the completion position (two blocks east of spawn)
                            BlockPos completionPos = nightmareLevel.getRespawnData().pos().offset(2, 0, 0);
                            nightmareLevel.setBlock(completionPos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                        } else {
                            player.displayClientMessage(Component.literal("§cNightmare dimension not found!"), false);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("§cYou are already awakened and cannot enter another First Nightmare."), false);
                    }
                }
            }
        });
    }
}