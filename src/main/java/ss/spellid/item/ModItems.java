package ss.spellid.item;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ss.spellid.TheSpell;
import ss.spellid.item.custom.DormantFragment;

public class ModItems {

    public static final Item DORMANT_FRAGMENT = register("dormant_fragment",
            DormantFragment::new, new Item.Properties());

    public static final ResourceKey<CreativeModeTab> CUSTOM_CREATIVE_TAB_KEY = ResourceKey.create(BuiltInRegistries
            .CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "creative_tab"));

    public static final CreativeModeTab CUSTOM_CREATIVE_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.DORMANT_FRAGMENT))
            .title(Component.translatable("itemGroup.the-spell"))
            .displayItems((params, output) -> {
                output.accept(DORMANT_FRAGMENT);
            })
            .build();

    private static <GenericItem extends Item> GenericItem register(String name, Function<Item.Properties
            , GenericItem> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create
                (Registries.ITEM, Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, name));
        GenericItem item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    @FunctionalInterface
    private interface ItemFactory{
        Item create(Item.Properties Properties);
    }

    public static void init() {

        TheSpell.LOGGER.info("Initializing Spell Mods");

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_CREATIVE_TAB_KEY, CUSTOM_CREATIVE_TAB);
    }

}