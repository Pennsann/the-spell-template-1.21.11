package ss.spellid.block;

import net.minecraft.core.Registry;
import ss.spellid.block.custom.CitadelGatewayBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import ss.spellid.TheSpell;

import java.util.function.Function;

public class ModBlocks {

    // List your blocks here
    public static final Block CITADEL_GATEWAY = registerBlock("citadel_gateway",
            CitadelGatewayBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());

    // Helper method to register a block and its item
    private static Block registerBlock(String name,
                                       Function<BlockBehaviour.Properties, Block> blockFactory,
                                       BlockBehaviour.Properties settings) {
        // Create block key
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, name));

        // Create block instance
        Block block = blockFactory.apply(settings.setId(blockKey));

        // Register block
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        // Register block item
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, name));
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void init() {
        TheSpell.LOGGER.info("Registering blocks for " + TheSpell.MOD_ID);
    }
}