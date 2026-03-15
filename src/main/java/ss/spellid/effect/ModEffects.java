package ss.spellid.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import ss.spellid.TheSpell;

public class ModEffects {
    public static final MobEffect NIGHTMARE_SEED_EFFECT = new NightmareSeedEffect();
    public static Holder<MobEffect> NIGHTMARE_SEED;

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "nightmare_seed");
        // Register the effect (so it has an ID and can be used in commands, etc.)
        Registry.register(BuiltInRegistries.MOB_EFFECT, id, NIGHTMARE_SEED_EFFECT);

        // Create a direct holder from the effect instance
        NIGHTMARE_SEED = Holder.direct(NIGHTMARE_SEED_EFFECT);

        TheSpell.LOGGER.info("Registered Nightmare Seed effect");
    }
}