package ss.spellid.components;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import ss.spellid.ranks.Ranks;

public interface EssenceComponent {
    int getCurrentEssence();
    void setCurrentEssence(int value);
    void addCurrentEssence(int amount);

    int getMaxEssence();

    int getSaturationProgress();
    int getSaturationMax();

    void absorbFragment();

    void readFromNBT(CompoundTag tag);
    void writeToNBT(CompoundTag tag);

    Ranks getRank();

    void setRank(Ranks newRank);

    void readFromNbt(CompoundTag tag);

    void writeToNbt(CompoundTag tag);

    void readData(ValueInput input);
    void writeData(ValueOutput output);

}
