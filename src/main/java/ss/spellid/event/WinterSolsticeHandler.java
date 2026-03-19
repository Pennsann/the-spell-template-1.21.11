package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import ss.spellid.TheSpell;
import ss.spellid.components.RankComponent;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.components.EssenceComponent;
import ss.spellid.dream.DreamRealmData;
import ss.spellid.dream.DreamRealmLoader;
import ss.spellid.ranks.Ranks;

import java.util.Random;
import java.util.Set;

public class WinterSolsticeHandler {
    private static final long DAYS_TO_WAIT = 3;
    private static final long TICKS_PER_DAY = 24000;
    private static final long REQUIRED_TICKS = DAYS_TO_WAIT * TICKS_PER_DAY;
    private static final int SPAWN_DISTANCE = 250; // exact distance from Citadel

    private static final ResourceKey<Level> DREAM_REALM_KEY = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm")
    );

    public static void register() {
        DreamRealmData.load();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                checkAndTeleportIfOverdue(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            checkAndTeleportIfOverdue(player);
        });
    }

    public static void forceTeleport(ServerPlayer player) {
        if (player.level().getServer() == null) return;
        var essence = RankComponentInitializer.ESSENCE.get(player);
        if (essence.isSentToDreamRealm()) return;
        teleportToDreamRealm(player);
        essence.setSentToDreamRealm(true);
    }

    private static void checkAndTeleportIfOverdue(ServerPlayer player) {
        if (player.isDeadOrDying()) return;

        RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(player);
        if (rankComp.getRank() != Ranks.SLEEPER) return;

        EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
        if (essence.isSentToDreamRealm()) return;
        if (essence.getSleeperStartTime() == 0) return;

        if (player.level().dimension().equals(DREAM_REALM_KEY)) return;

        long currentTime = player.level().getServer().overworld().getGameTime();
        if (currentTime - essence.getSleeperStartTime() >= REQUIRED_TICKS) {
            teleportToDreamRealm(player);
            essence.setSentToDreamRealm(true);
        }
    }

    private static void teleportToDreamRealm(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();

        DreamRealmLoader.ensureDimensionFilesExist(server);

        ServerLevel dreamRealm = server.getLevel(DREAM_REALM_KEY);
        if (dreamRealm == null) {
            TheSpell.LOGGER.error("Dream Realm dimension not found!");
            return;
        }

        // Get a random Citadel position from the JSON list
        BlockPos citadel = DreamRealmData.getRandomCitadel();

        // Compute a random point 250 blocks away from the chosen citadel
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        int dx = (int) (Math.cos(angle) * SPAWN_DISTANCE);
        int dz = (int) (Math.sin(angle) * SPAWN_DISTANCE);
        int targetX = citadel.getX() + dx;
        int targetZ = citadel.getZ() + dz;

        // Find a safe spawn location near (targetX, targetZ)
        BlockPos safePos = findSafeSpawnNear(dreamRealm, targetX, targetZ, citadel);
        if (safePos == null) {
            TheSpell.LOGGER.warn("Could not find safe spawn near {},{}; falling back to citadel", targetX, targetZ);
            safePos = citadel.above(); // at least citadel is safe (should be)
        }

        // Ensure chunk is loaded
        dreamRealm.getChunk(safePos.getX() >> 4, safePos.getZ() >> 4);

        player.teleportTo(
                dreamRealm,
                safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                false
        );

        // Set anchor in EssenceComponent
        EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
        essence.setAnchor(citadel.getX(), citadel.getY(), citadel.getZ());
        essence.setSentToDreamRealm(true);

        player.displayClientMessage(
                Component.literal("§5The Winter Solstice has come. You find yourself in the Dream Realm, far from any Citadel..."),
                false
        );
        TheSpell.LOGGER.info("Player {} teleported to Dream Realm (Winter Solstice) near citadel {}, safe spawn {}",
                player.getName().getString(), citadel, safePos);
    }

    private static BlockPos findSafeSpawnNear(ServerLevel level, int x, int z, BlockPos fallbackPos) {
        // First, try the exact target position
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, surfaceY, z);

        if (isSafeSpawn(level, pos)) {
            return pos.immutable();
        }

        // If not, try scanning up/down a few blocks from surfaceY
        for (int dy = 0; dy <= 5; dy++) {
            if (isSafeSpawn(level, pos.set(x, surfaceY + dy, z))) {
                return pos.immutable();
            }
            if (dy > 0 && isSafeSpawn(level, pos.set(x, surfaceY - dy, z))) {
                return pos.immutable();
            }
        }

        // Still no safe spot? Try adjacent blocks in a 3x3 grid
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                int adjX = x + dx;
                int adjZ = z + dz;
                int adjY = level.getHeight(Heightmap.Types.WORLD_SURFACE, adjX, adjZ);
                if (isSafeSpawn(level, pos.set(adjX, adjY, adjZ))) {
                    return pos.immutable();
                }
            }
        }

        return null;
    }

    private static boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos.below()).isAir()) return false; // need solid ground
        if (!level.getBlockState(pos).canBeReplaced()) return false; // feet block must be air/replaceable
        if (!level.getBlockState(pos.above()).canBeReplaced()) return false; // head block must be air/replaceable
        return true;
    }
}