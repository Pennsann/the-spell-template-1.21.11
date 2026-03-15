package ss.spellid.event;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import ss.spellid.TheSpell;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.effect.ModEffects;
import ss.spellid.ranks.Ranks;

import java.util.Set;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class SleepHandler {
    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayer player) {
                TheSpell.LOGGER.info("===== SLEEP EVENT STARTED =====");
                TheSpell.LOGGER.info("Player: " + player.getName().getString());

                var essence = ESSENCE.get(player);
                boolean hasSeed = essence.hasNightmareSeed();
                TheSpell.LOGGER.info("hasNightmareSeed from component: " + hasSeed);

                // 2% chance to get nightmare seed (only if they don't have it)
                if (!hasSeed && player.getRandom().nextFloat() < 0.02f) {
                    essence.setNightmareSeed(true);
                    // Also give the effect
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ModEffects.NIGHTMARE_SEED,
                            -1, // infinite
                            0,
                            false,
                            true,
                            true
                    ));
                    player.displayClientMessage(Component.literal("§5You feel a strange seed taking root in your soul..."), false);
                    TheSpell.LOGGER.info("Randomly applied nightmare seed (component + effect).");
                }

                // If they have the seed and are PLAYER, trigger First Nightmare
                if (hasSeed) {
                    var rankComp = RANK_KEY.get(player);
                    Ranks currentRank = rankComp.getRank();
                    TheSpell.LOGGER.info("Player rank: " + currentRank);
                    if (currentRank == Ranks.PLAYER) {
                        TheSpell.LOGGER.info("Conditions met. Consuming seed and attempting teleport...");
                        essence.setNightmareSeed(false);
                        player.removeEffect(ModEffects.NIGHTMARE_SEED); // Remove effect

                        Identifier dimensionId = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "first_nightmare");
                        ResourceKey<Level> nightmareKey = ResourceKey.create(
                                Registries.DIMENSION,
                                dimensionId
                        );

                        TheSpell.LOGGER.info("Looking for dimension with key: " + nightmareKey);
                        ServerLevel nightmareLevel = player.level().getServer().getLevel(nightmareKey);
                        TheSpell.LOGGER.info("Nightmare level found: " + (nightmareLevel != null));

                        if (nightmareLevel != null) {
                            double x = nightmareLevel.getRespawnData().pos().getX();
                            double y = nightmareLevel.getRespawnData().pos().getY();
                            double z = nightmareLevel.getRespawnData().pos().getZ();
                            TheSpell.LOGGER.info("Teleport coordinates: " + x + ", " + y + ", " + z);

                            player.teleportTo(
                                    nightmareLevel,
                                    x, y, z,
                                    Set.of(),
                                    player.getYRot(),
                                    player.getXRot(),
                                    false
                            );
                            player.displayClientMessage(Component.literal("§5You awaken in a strange, empty realm... the First Nightmare begins!"), false);
                            TheSpell.LOGGER.info("Teleport command executed.");
                        } else {
                            TheSpell.LOGGER.error("Nightmare dimension not found! Check JSON files.");
                        }
                    } else {
                        TheSpell.LOGGER.info("Player rank is not PLAYER, skipping teleport.");
                    }
                } else {
                    TheSpell.LOGGER.info("Player does NOT have nightmare seed, no teleport.");
                }
                TheSpell.LOGGER.info("===== SLEEP EVENT ENDED =====\n");
            }
        });
    }
}