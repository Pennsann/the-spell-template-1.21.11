package ss.spellid.components;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.TheSpell;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public class EssenceComponentImpl implements EssenceComponent {
    private static final int SATURATION_STEPS = 100;
    private static final float TARGET_BONUS = 0.5f;
    private static final int MICRO_PER_POINT = 100;

    private int currentEssence = 0;
    private int storedMicroPoints = 0;
    private int saturationProgress = 0;
    private Ranks rank = Ranks.PLAYER;

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
        Ranks rank = RankComponentInitializer.RANK_KEY.get(entity).getRank();
        if(!rank.hasSoulCore()){
            return 0;
        }
        int base = rank.getBaseMaxEssence();
        float bonus = TARGET_BONUS *(saturationProgress/(float)SATURATION_STEPS);
        return (int)(base * (1 + bonus));
    }

    @Override
    public int getSaturationProgress() {
        return saturationProgress;
    }

    @Override
    public int getSaturationMax() {
        return SATURATION_STEPS;
    }

    @Override
    public void absorbFragment(FragmentTier fragment) {
        if(saturationProgress >= SATURATION_STEPS){
            TheSpell.LOGGER.info("Already saturated!");
            return;
        }

        Ranks currentRank = RankComponentInitializer.RANK_KEY.get(entity).getRank();

        double pointsGained = currentRank.getAbsorptionEfficiencyForFragmentTier(fragment);

        // Add debug logging
        TheSpell.LOGGER.info("=== Absorption Debug ===");
        TheSpell.LOGGER.info("Current rank: " + currentRank);
        TheSpell.LOGGER.info("Fragment tier: " + fragment);
        TheSpell.LOGGER.info("Points gained: " + pointsGained);
        TheSpell.LOGGER.info("Before - Stored micro: " + storedMicroPoints + ", Saturation: " + saturationProgress);

        if (pointsGained <= 0) {
            TheSpell.LOGGER.info("No points gained, skipping");
            return;
        }else {
            int microToAdd = (int) (pointsGained * MICRO_PER_POINT);
            storedMicroPoints += microToAdd;

            int newPoints = storedMicroPoints / MICRO_PER_POINT;
            storedMicroPoints %= MICRO_PER_POINT;

            if (newPoints > 0) {
                saturationProgress += newPoints;
                TheSpell.LOGGER.info("Added " + newPoints + " to saturation, now: " + saturationProgress);
            }
            TheSpell.LOGGER.info("Remaining micro-points: " + storedMicroPoints);
        }

        if (saturationProgress > SATURATION_STEPS) {
            saturationProgress = SATURATION_STEPS;
        }
        TheSpell.LOGGER.info("After - Saturation: " + saturationProgress);
    }

    @Override
    public Ranks getRank(){
        return rank;
    }

    @Override
    public void setRank(Ranks newRank) {
        this.rank = newRank != null ? newRank : Ranks.PLAYER;
    }

    @Override
    public void readData(ValueInput input) {
        TheSpell.LOGGER.info("Loading Essence data from ValueInput");

        // Read using the correct methods
        currentEssence = input.getInt("CurrentEssence").orElse(0);
        storedMicroPoints = input.getInt("StoredMicroPoints").orElse(0);
        saturationProgress = input.getInt("SaturationProgress").orElse(0);

        String rankName = input.getString("Rank").orElse("");
        try {
            rank = Ranks.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            rank = Ranks.PLAYER;
        }

        TheSpell.LOGGER.info("Loaded: essence=" + currentEssence +
                ", micro=" + storedMicroPoints +
                ", sat=" + saturationProgress +
                ", rank=" + rank);
    }

    @Override
    public void writeData(ValueOutput output) {
        TheSpell.LOGGER.info("Saving Essence data to ValueOutput");

        // Write using the correct methods (assuming ValueOutput has putInt/putString)
        output.putInt("CurrentEssence", currentEssence);
        output.putInt("StoredMicroPoints", storedMicroPoints);
        output.putInt("SaturationProgress", saturationProgress);
        output.putString("Rank", rank.name());

        TheSpell.LOGGER.info("Saved: essence=" + currentEssence +
                ", micro=" + storedMicroPoints +
                ", sat=" + saturationProgress +
                ", rank=" + rank);
    }
}
