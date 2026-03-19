package ss.spellid.dream;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import ss.spellid.TheSpell;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class DreamRealmLoader {
    private static final ResourceKey<Level> DREAM_REALM_KEY = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm")
    );

    private static final String DIMENSION_FOLDER_PATH = "dimensions/" + TheSpell.MOD_ID + "/dream_realm";
    private static final String PRESET_PATH = "data/" + TheSpell.MOD_ID + "/dimension_preset/dream_realm";

    public static void ensureDimensionFilesExist(MinecraftServer server) {
        Path worldSaveRoot = server.getWorldPath(LevelResource.ROOT);
        Path targetDimFolder = worldSaveRoot.resolve(DIMENSION_FOLDER_PATH);

        TheSpell.LOGGER.info("Checking Dream Realm dimension folder at: {}", targetDimFolder);

        // If the folder exists and contains region files, assume it's already populated
        if (Files.exists(targetDimFolder) && hasRegionFiles(targetDimFolder)) {
            TheSpell.LOGGER.info("Dream Realm dimension folder already contains region files, skipping copy.");
            return;
        }

        // If folder exists but is empty (no region files), we still copy
        if (Files.exists(targetDimFolder)) {
            TheSpell.LOGGER.info("Dream Realm dimension folder exists but is empty. Will copy pre-built files.");
        } else {
            TheSpell.LOGGER.info("Dream Realm dimension folder not found. Copying pre-built files...");
        }

        // Find the source folder inside the mod JAR
        Path sourcePreset = FabricLoader.getInstance().getModContainer(TheSpell.MOD_ID)
                .flatMap(container -> container.findPath(PRESET_PATH))
                .orElse(null);

        if (sourcePreset == null) {
            TheSpell.LOGGER.error("Pre-built Dream Realm files not found in mod JAR at path: {}", PRESET_PATH);
            TheSpell.LOGGER.error("Make sure the folder exists at: src/main/resources/{}", PRESET_PATH);
            return;
        }

        TheSpell.LOGGER.info("Found source preset at: {}", sourcePreset);

        // List contents of source preset to verify
        try (Stream<Path> walk = Files.walk(sourcePreset)) {
            TheSpell.LOGGER.info("Contents of source preset:");
            walk.forEach(path -> {
                if (!path.equals(sourcePreset)) {
                    TheSpell.LOGGER.info("  - {}", sourcePreset.relativize(path));
                }
            });
        } catch (IOException e) {
            TheSpell.LOGGER.error("Failed to list source preset contents", e);
        }

        try {
            // Create the target directory if it doesn't exist
            Files.createDirectories(targetDimFolder);

            // Copy everything recursively from source to target
            Files.walkFileTree(sourcePreset, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path target = targetDimFolder.resolve(sourcePreset.relativize(dir));
                    Files.createDirectories(target);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path target = targetDimFolder.resolve(sourcePreset.relativize(file));
                    TheSpell.LOGGER.info("Copying file: {} -> {}", file, target);
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });

            TheSpell.LOGGER.info("Dream Realm dimension files copied successfully.");
        } catch (IOException e) {
            TheSpell.LOGGER.error("Failed to copy Dream Realm dimension files", e);
        }
    }

    private static boolean hasRegionFiles(Path folder) {
        Path regionFolder = folder.resolve("region");
        if (!Files.exists(regionFolder)) return false;
        try (Stream<Path> files = Files.list(regionFolder)) {
            return files.anyMatch(path -> path.toString().endsWith(".mca"));
        } catch (IOException e) {
            return false;
        }
    }
}