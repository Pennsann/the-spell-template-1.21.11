package ss.spellid.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ss.spellid.TheSpell;
import ss.spellid.block.entity.NightmareSeedBlockEntity;

import java.util.Set;

public class ModBlockEntities {
    public static final BlockEntityType<NightmareSeedBlockEntity> NIGHTMARE_SEED_BE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "nightmare_seed"),
                    FabricBlockEntityTypeBuilder.create(NightmareSeedBlockEntity::new, ModBlocks.NIGHTMARE_SEED).build()
            );
    public static void register() {

    }
}