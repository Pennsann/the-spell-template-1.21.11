package ss.spellid.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import ss.spellid.TheSpell;

import java.util.Optional;

public class DreamRealmHandler {
    private static final ResourceKey<Level> DREAM_REALM_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm"));
    private static final Identifier STRUCTURE_LOCATION =
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "dream_realm");
    private static boolean hasPastedStructure = false;

    public static void ensureMapPlaced(ServerLevel level) {
        if (!level.dimension().equals(DREAM_REALM_KEY)) return;
        if (hasPastedStructure) return;

        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> templateOpt = manager.get(STRUCTURE_LOCATION);

        if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(false);

            BlockPos spawnPos = level.getRespawnData().pos();
            template.placeInWorld(level, spawnPos, spawnPos, settings, level.random, 3);
            hasPastedStructure = true;
            TheSpell.LOGGER.info("Dream Realm map placed at spawn.");
        } else {
            TheSpell.LOGGER.error("Dream Realm structure not found!");
        }
    }
}