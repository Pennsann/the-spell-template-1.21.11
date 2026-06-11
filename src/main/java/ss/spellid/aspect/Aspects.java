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
import ss.spellid.aspect.ability.fire.FireballAbility;
import ss.spellid.aspect.ability.frost.FrigidTorrentAbility;
import ss.spellid.aspect.ability.frost.PermafrostAuraPower;
import ss.spellid.aspect.ability.frost.PermafrostTouchAbility;
import ss.spellid.aspect.definition.FireAspect;
import ss.spellid.aspect.definition.FrostOfTheLonelyPeakAspect;
import ss.spellid.aspect.definition.SurvivorAspect;

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


    public static final Aspect SURVIVOR = register(SurvivorAspect.INSTANCE);
    public static final Aspect FIRE = register(FireAspect.INSTANCE);
    public static final Aspect FROST_OF_THE_LONELY_PEAK = register(FrostOfTheLonelyPeakAspect.INSTANCE);

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