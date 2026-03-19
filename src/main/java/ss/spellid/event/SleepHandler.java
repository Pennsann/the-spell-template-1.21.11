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
import ss.spellid.effect.ModEffects;
import ss.spellid.nightmare.Nightmare;
import ss.spellid.nightmare.NightmareManager;
import ss.spellid.ranks.Ranks;

import java.util.Set;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class SleepHandler {
    private static final ResourceKey<Level> DREAM_REALM_KEY = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm")
    );

    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayer player) {
                var rankComp = RANK_KEY.get(player);
                var essence = ESSENCE.get(player);
                Ranks currentRank = rankComp.getRank();

                // 1. Seed chance for PLAYER
                if (currentRank == Ranks.PLAYER) {
                    boolean hasSeed = essence.hasNightmareSeed();
                    if (!hasSeed && player.getRandom().nextFloat() < 0.02f) {
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

                // 2. If PLAYER with seed, enter First Nightmare
                if (currentRank == Ranks.PLAYER && essence.hasNightmareSeed()) {
                    Identifier nightmareId = NightmareManager.getRandomUncompletedSolo(player);
                    if (nightmareId == null) {
                        player.displayClientMessage(Component.literal("§cNo nightmares remain..."), false);
                        return;
                    }
                    Nightmare nightmare = NightmareManager.get(nightmareId);
                    if (nightmare == null) return;
                    ResourceKey<Level> nightmareKey = nightmare.dimensionKey();

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

                        BlockPos completionPos = nightmareLevel.getRespawnData().pos().offset(2, 0, 0);
                        nightmareLevel.setBlock(completionPos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                    } else {
                        player.displayClientMessage(Component.literal("§cNightmare dimension not found!"), false);
                    }
                    return; // exit after nightmare entry
                }

                // 3. Re‑entry for ranks with a Dream Realm anchor (SLEEPER, AWAKENED, etc.)
                if (currentRank.hasSoulCore() && essence.isSentToDreamRealm()) {
                    // Don't teleport if already in Dream Realm
                    if (player.level().dimension().equals(DREAM_REALM_KEY)) {
                        player.displayClientMessage(Component.literal("§7You are already in the Dream Realm."), false);
                        return;
                    }

                    if (essence.hasAnchor()) {
                        ServerLevel dreamRealm = player.level().getServer().getLevel(DREAM_REALM_KEY);
                        if (dreamRealm != null) {
                            BlockPos anchorPos = new BlockPos(essence.getAnchorX(), essence.getAnchorY(), essence.getAnchorZ());
                            player.teleportTo(
                                    dreamRealm,
                                    anchorPos.getX() + 0.5, anchorPos.getY(), anchorPos.getZ() + 0.5,
                                    Set.of(),
                                    player.getYRot(),
                                    player.getXRot(),
                                    false
                            );
                            String rankName = currentRank.getDisplayName();
                            player.displayClientMessage(Component.literal("§5As a " + rankName + ", you slip into the Dream Realm through slumber..."), false);
                        } else {
                            player.displayClientMessage(Component.literal("§cDream Realm dimension not found!"), false);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("§cYou have no anchor in the Dream Realm. Seek a Citadel Gateway."), false);
                    }
                    return;
                }

                // 4. For others (PLAYER without seed, or any other case), normal sleep
                player.displayClientMessage(Component.literal("§7You sleep peacefully."), false);
            }
        });
    }
}