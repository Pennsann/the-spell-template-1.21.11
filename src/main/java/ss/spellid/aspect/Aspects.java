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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aspects {
    private static final Map<Identifier, Aspect> REGISTRY = new HashMap<>();

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
                    )
            )
    );

    public static final Aspect FIRE = register(
            new Aspect(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "fire"),
                    Component.literal("Fire Aspect"),
                    Component.literal("Fire resistance, sets attackers on fire"),
                    new ItemStack(Items.BLAZE_POWDER),
                    List.of(
                            new PotionEffectPower(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false, true)),
                            new AttributeModifierPower(
                                    BuiltInRegistries.ATTRIBUTE.getKey(Attributes.ATTACK_DAMAGE.value()),
                                    0.5,
                                    AttributeModifier.Operation.ADD_VALUE,
                                    "fire_attack"
                            )
                    )
            )
    );

    public static final Aspect FLIGHT = register(
            new Aspect(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "flight"),
                    Component.literal("Flight Aspect"),
                    Component.literal("Grants creative flight (WIP)"),
                    new ItemStack(Items.FEATHER),
                    List.of()
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
        // For now just return SURVIVOR
        return SURVIVOR.getId();
    }

    public static void init() {
        // Just to load the class
    }
}