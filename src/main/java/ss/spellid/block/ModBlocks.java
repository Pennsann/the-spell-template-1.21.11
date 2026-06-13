package ss.spellid.block;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.SoundType;
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
import ss.spellid.block.custom.IceSheetBlock;
import ss.spellid.block.custom.NightmareSeedBlock;
import ss.spellid.item.custom.NightmareSeedItem;

import java.util.function.Function;

public class ModBlocks {

    // List your blocks here
    public static final Block CITADEL_GATEWAY = registerBlock("citadel_gateway",
            CitadelGatewayBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());

    public static final Block ICE_SHEET = registerBlock("ice_sheet",
            IceSheetBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(0.2f)
                    .noOcclusion()
                    .friction(0.98f)    // slippery
                    .sound(SoundType.GLASS)  // ice‑like break sound
    );

    public static final Block NIGHTMARE_SEED = registerBlock("nightmare_seed",
            NightmareSeedBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion(),
            false  // skip auto item registration
    );

    // Helper method to register a block and its item
    private static Block registerBlock(String name,
                                       Function<BlockBehaviour.Properties, Block> blockFactory,
                                       BlockBehaviour.Properties settings) {
        return registerBlock(name, blockFactory, settings, true);
    }

    private static Block registerBlock(String name,
                                       Function<BlockBehaviour.Properties, Block> blockFactory,
                                       BlockBehaviour.Properties settings,
                                       boolean registerItem) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, name));
        Block block = blockFactory.apply(settings.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        if (registerItem) {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, name));
            BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }

        return block;
    }

    public static void registerCustomItems() {
        ResourceKey<Item> seedItemKey = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "nightmare_seed"));
        NightmareSeedItem seedItem = new NightmareSeedItem(
                new Item.Properties().setId(seedItemKey));
        Registry.register(BuiltInRegistries.ITEM, seedItemKey, seedItem);
    }

    public static void init() {
        TheSpell.LOGGER.info("Registering blocks for " + TheSpell.MOD_ID);
        TheSpell.LOGGER.info("Registering blocks for " + TheSpell.MOD_ID);
        registerCustomItems();
    }
}