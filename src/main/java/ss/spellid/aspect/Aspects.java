package ss.spellid.aspect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ss.spellid.TheSpell;
import ss.spellid.aspect.ability.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aspects {
    private static final Map<Identifier, Aspect> REGISTRY = new HashMap<>();

    // Helper to create a mutable list of 3 abilities (slot 0,1,2)
    private static List<AspectAbility> createAbilityList(AspectAbility slot0, AspectAbility slot1, AspectAbility slot2) {
        List<AspectAbility> list = new ArrayList<>(3);
        list.add(slot0);
        list.add(slot1);
        list.add(slot2);
        return list;
    }

    // Survivor – no abilities
    public static final Aspect SURVIVOR = register(
            new Aspect(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "survivor"),
                    Component.literal("Survivor"),
                    Component.literal("+2 hearts, +5% speed"),
                    new ItemStack(Items.APPLE),
                    List.of(
                            new AttributeModifierPower(
                                    BuiltInRegistries.ATTRIBUTE.getKey(Attributes.MAX_HEALTH.value()),
                                    4.0,
                                    AttributeModifier.Operation.ADD_VALUE,
                                    "survivor_health"
                            ),
                            new AttributeModifierPower(
                                    BuiltInRegistries.ATTRIBUTE.getKey(Attributes.MOVEMENT_SPEED.value()),
                                    0.005,
                                    AttributeModifier.Operation.ADD_VALUE,
                                    "survivor_speed"
                            )
                    ),
                    new ArrayList<>()
            )
    );

    // Fire Aspect – ability at slot 1 (Awakened)
    public static final Aspect FIRE = register(
            new Aspect(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "fire"),
                    Component.literal("Fire Aspect"),
                    Component.literal("Fire resistance, sets attackers on fire, and shoot fireballs"),
                    new ItemStack(Items.BLAZE_POWDER),
                    List.of(
                            new PotionEffectPower(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false, true)),
                            new AttributeModifierPower(
                                    BuiltInRegistries.ATTRIBUTE.getKey(Attributes.ATTACK_DAMAGE.value()),
                                    0.5,
                                    AttributeModifier.Operation.ADD_VALUE,
                                    "fire_attack"
                            )
                    ),
                    createAbilityList(null, new FireballAbility(), null)
            )
    );

    // Flight – no abilities
    public static final Aspect FLIGHT = register(
            new Aspect(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "flight"),
                    Component.literal("Flight Aspect"),
                    Component.literal("Grants creative flight (WIP)"),
                    new ItemStack(Items.FEATHER),
                    List.of(),
                    new ArrayList<>()
            )
    );

    // Frost of the Lonely Peak – ability at slot 0 (Dormant)
    public static final Aspect FROST_OF_THE_LONELY_PEAK = register(
            new Aspect(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "frost_of_the_lonely_peak"),
                    Component.literal("Frost of the Lonely Peak"),
                    Component.literal("The cold of the highest mountain, where even thoughts freeze."),
                    new ItemStack(Items.PACKED_ICE),
                    List.of(new PermafrostAuraPower()),
                    createAbilityList(new PermafrostTouchAbility(), new FrigidTorrentAbility(), null)
            )
    );

    private static Aspect register(Aspect aspect) {
        REGISTRY.put(aspect.getId(), aspect);
        return aspect;
    }

    public static Aspect get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Identifier getRandomStarterId() {
        return SURVIVOR.getId();
    }

    public static void init() {
        // loads class
    }
}