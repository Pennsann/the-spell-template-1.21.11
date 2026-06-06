package ss.spellid.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import ss.spellid.TheSpell;

public class ModEffects {
    public static final MobEffect NIGHTMARE_SEED_EFFECT = new NightmareSeedEffect();
    public static final MobEffect CHILLED = new ChilledEffect();
    public static final MobEffect FROZEN = new FrozenEffect();

    public static Holder<MobEffect> NIGHTMARE_SEED;
    public static Holder<MobEffect> CHILLED_HOLDER;
    public static Holder<MobEffect> FROZEN_HOLDER;

    public static void register() {
        NIGHTMARE_SEED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "nightmare_seed"), NIGHTMARE_SEED_EFFECT);
        CHILLED_HOLDER = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "chilled"), CHILLED);
        FROZEN_HOLDER = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "frozen"), FROZEN);
    }
}