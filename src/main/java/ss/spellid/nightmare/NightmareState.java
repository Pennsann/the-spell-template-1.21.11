package ss.spellid.nightmare;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import ss.spellid.TheSpell;

import java.util.HashSet;
import java.util.Set;

public class NightmareState extends SavedData {
    private static final String DATA_NAME = TheSpell.MOD_ID + "_nightmares";
    private final Set<String> completedNightmares = new HashSet<>();

    // Codec for serializing/deserializing the set of strings
    private static final Codec<Set<String>> SET_CODEC = Codec.STRING.listOf().xmap(
            HashSet::new,
            list -> list.stream().toList()
    );

    // Codec for the whole state
    private static final Codec<NightmareState> CODEC = SET_CODEC.xmap(
            NightmareState::new,
            state -> state.completedNightmares
    );

    private static final SavedDataType<NightmareState> TYPE = new SavedDataType<>(
            DATA_NAME,
            NightmareState::new,
            CODEC,
            null // Data fixer (not needed)
    );

    public NightmareState() {
    }

    private NightmareState(Set<String> completed) {
        this.completedNightmares.addAll(completed);
    }

    public boolean isCompleted(Identifier id) {
        return completedNightmares.contains(id.toString());
    }

    public void setCompleted(Identifier id) {
        completedNightmares.add(id.toString());
        setDirty();
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this)
                .getOrThrow(string -> new RuntimeException("Failed to encode: " + string));
    }

    public static NightmareState load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(string -> new RuntimeException("Failed to decode: " + string));
    }

    public static NightmareState getOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}