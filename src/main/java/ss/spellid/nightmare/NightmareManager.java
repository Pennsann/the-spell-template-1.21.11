package ss.spellid.nightmare;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import ss.spellid.TheSpell;
import ss.spellid.aspect.Aspects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class NightmareManager {
    private static final Map<Identifier, Nightmare> REGISTRY = new HashMap<>();

    // Define multiple First Nightmares with their own dimensions and aspects
    public static final Nightmare FLAMING_TRIAL = register(new Nightmare(
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "flaming_trial"),
            "Trial of Flames",
            Nightmare.EntryType.SLEEP,
            1, 1,
            ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "flaming_trial")),
            Aspects.FIRE.getId() // Grant Fire Aspect
    ));

    public static final Nightmare FROZEN_TRIAL = register(new Nightmare(
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "frozen_trial"),
            "Trial of Frost",
            Nightmare.EntryType.SLEEP,
            1, 1,
            ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "frozen_trial")),
            Aspects.SURVIVOR.getId() // Placeholder – replace with actual frost aspect later
    ));

    public static final Nightmare SHADOW_TRIAL = register(new Nightmare(
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "shadow_trial"),
            "Trial of Shadows",
            Nightmare.EntryType.SLEEP,
            1, 1,
            ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "shadow_trial")),
            Aspects.SURVIVOR.getId() // Placeholder
    ));

    // Keep test nightmare for backward compatibility
    public static final Nightmare TEST_NIGHTMARE = register(new Nightmare(
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "test_nightmare"),
            "Test Nightmare",
            Nightmare.EntryType.SLEEP,
            1, 1,
            ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "first_nightmare")),
            Aspects.SURVIVOR.getId()
    ));

    private static Nightmare register(Nightmare nightmare) {
        REGISTRY.put(nightmare.id(), nightmare);
        return nightmare;
    }

    public static Nightmare get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static List<Nightmare> getAllSolo() {
        return REGISTRY.values().stream()
                .filter(n -> n.entryType() == Nightmare.EntryType.SLEEP)
                .collect(Collectors.toList());
    }

    public static Identifier getRandomUncompletedSolo(ServerPlayer player) {
        NightmareState state = NightmareState.getOrCreate(player.level().getServer());
        List<Nightmare> uncompleted = getAllSolo().stream()
                .filter(n -> !state.isCompleted(n.id()))
                .collect(Collectors.toList());
        if (uncompleted.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return uncompleted.get(random.nextInt(uncompleted.size())).id();
    }

    public static void complete(Identifier id, ServerPlayer player) {
        NightmareState state = NightmareState.getOrCreate(player.level().getServer());
        state.setCompleted(id);
    }
}