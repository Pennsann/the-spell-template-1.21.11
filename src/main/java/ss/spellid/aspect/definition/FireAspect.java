package ss.spellid.aspect.definition;

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
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.AttributeModifierPower;
import ss.spellid.aspect.PotionEffectPower;
import ss.spellid.aspect.ability.fire.FireballAbility;

import java.util.List;

public class FireAspect {
    public static final Aspect INSTANCE = new Aspect(
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
            List.of(
                    null,                    // slot 0 - Sleeper (none)
                    new FireballAbility(),   // slot 1 - Awakened
                    null                     // slot 2 - Ascended (not yet)
            )
    );
}
