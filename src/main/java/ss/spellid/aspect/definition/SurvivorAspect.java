package ss.spellid.aspect.definition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ss.spellid.TheSpell;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.AttributeModifierPower;

import java.util.List;

public class SurvivorAspect {
    public static final Aspect INSTANCE = new Aspect(
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
    );
}
