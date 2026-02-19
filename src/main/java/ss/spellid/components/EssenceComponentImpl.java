package ss.spellid.components;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.ModComponents;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public class EssenceComponentImpl implements EssenceComponent {
    private static final int SATURATION_STEPS = 100;
    private static final float TARGET_BONUS = 0.5f;
    private static final int MICRO_PER_POINT = 100;

    private int currentEssence = 0;
    private  int storedMicroPoints = 0;
    private int saturationProgress = 0;

    private final Entity entity;

    public EssenceComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public int getCurrentEssence() {
        return currentEssence;
    }

    @Override
    public void setCurrentEssence(int value) {
        currentEssence = Math.max(0, Math.min(value, getMaxEssence()));
    }

    @Override
    public void addCurrentEssence(int amount) {
        setCurrentEssence(getCurrentEssence() + amount);
    }

    @Override
    public int getMaxEssence() {
        Ranks rank = ModComponents.RANK.get(entity).getRank();
        if(rank.hasSoulCore()){
            return 0;
        }
        int base = rank.getBaseMaxEssence();
        float bonus = TARGET_BONUS *(SATURATION_STEPS/(float)SATURATION_STEPS);
        return (int)(base * (1 + bonus));
    }

    @Override
    public int getSaturationProgress() {
        return saturationProgress;
    }

    @Override
    public int getSaturationMax() {  // IDK WHAT THIS IS
        return 0;
    }

    @Override
    public void absorbFragment() {
        Ranks currentRank = ModComponents.RANK.get(entity).getRank();
        FragmentTier fragmentRank = FragmentTier.DORMANT;

        double pointsGained = currentRank.getAbsorptionEfficiencyForFragmentTier(fragmentRank);

        if(pointsGained <= 0){
            return;
        }

        int microToAdd = (int) (pointsGained * MICRO_PER_POINT);
        storedMicroPoints += microToAdd;

        int newPoints = storedMicroPoints / MICRO_PER_POINT;
        storedMicroPoints %= MICRO_PER_POINT;

        saturationProgress += Math.min(SATURATION_STEPS, saturationProgress + newPoints);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        currentEssence = tag.getInt("CurrentEssence").orElse(0);
        storedMicroPoints = tag.getInt("StoredMicroPoints").orElse(0);
        saturationProgress = tag.getInt("SaturationProgress").orElse(0);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("CurrentEssence", currentEssence);
        tag.putInt("StoredMicroPoints", storedMicroPoints);
        tag.putInt("SaturationProgress", saturationProgress);
    }

    @Override
    public Ranks getRank() {               // IDK WHAT THIS IS
        return null;
    }

    @Override
    public void setRank(Ranks newRank) { // IDK WHAT THIS IS

    }

    @Override
    public void readFromNbt(CompoundTag tag) {  // IDK WHAT THIS IS

    }

    @Override
    public void writeToNbt(CompoundTag tag) {  // IDK WHAT THIS IS

    }

    @Override
    public void readData(ValueInput valueInput) {}

    @Override
    public void writeData(ValueOutput valueOutput) {}
}
