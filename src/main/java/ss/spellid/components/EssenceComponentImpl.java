package ss.spellid.components;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.TheSpell;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public class EssenceComponentImpl implements EssenceComponent {
    private static final int SATURATION_STEPS = 1000;
    private static final float TARGET_BONUS = 0.5f;
    private static final int MICRO_PER_POINT = 100;

    // Saturation modifier identifiers
    private static final Identifier SATURATION_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "saturation_health");
    private static final Identifier SATURATION_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "saturation_speed");
    private static final Identifier SATURATION_ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "saturation_attack");

    private int currentEssence = 0;
    private int storedMicroPoints = 0;
    private int saturationProgress = 0;
    private Ranks rank = Ranks.PLAYER;
    private boolean hasNightmareSeed = false;

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
        if (!rank.hasSoulCore()) {
            return 0;
        }
        int base = rank.getBaseMaxEssence();
        float bonus = TARGET_BONUS * (saturationProgress / (float) SATURATION_STEPS);
        return (int) (base * (1 + bonus));
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
        if (saturationProgress >= SATURATION_STEPS) {
            TheSpell.LOGGER.info("Already saturated!");
            return;
        }

        Ranks currentRank = RankComponentInitializer.RANK_KEY.get(entity).getRank();
        double pointsGained = currentRank.getAbsorptionEfficiencyForFragmentTier(fragment);

        TheSpell.LOGGER.info("=== Absorption Debug ===");
        TheSpell.LOGGER.info("Current rank: " + currentRank);
        TheSpell.LOGGER.info("Fragment tier: " + fragment);
        TheSpell.LOGGER.info("Points gained: " + pointsGained);
        TheSpell.LOGGER.info("Before - Stored micro: " + storedMicroPoints + ", Saturation: " + saturationProgress);

        if (pointsGained <= 0) {
            TheSpell.LOGGER.info("No points gained, skipping");
            return;
        }

        // Handle whole points directly
        if (pointsGained == 1.0 || pointsGained == 5.0) {
            saturationProgress += (int) pointsGained;
            TheSpell.LOGGER.info("Added " + (int) pointsGained + " whole points directly");
        } else {
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

        // Update attribute modifiers based on new saturation
        updateSaturationModifiers();
    }

    @Override
    public Ranks getRank() {
        return rank;
    }

    @Override
    public void setRank(Ranks newRank) {
        this.rank = newRank != null ? newRank : Ranks.PLAYER;
    }

    @Override
    public boolean hasNightmareSeed() {
        return hasNightmareSeed;
    }

    @Override
    public void setNightmareSeed(boolean hasSeed) {
        this.hasNightmareSeed = hasSeed;
    }

    /**
     * Updates the player's attribute bonuses based on current saturation.
     * The bonuses scale linearly from 0 to the max values as saturation increases.
     */
    @Override
    public void updateSaturationModifiers() {
        if (!(entity instanceof Player player)) return;

        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (healthAttr == null || speedAttr == null || attackAttr == null) {
            TheSpell.LOGGER.warn("Could not get player attributes for saturation modifiers");
            return;
        }

        // Remove old modifiers
        healthAttr.removeModifier(SATURATION_HEALTH_MODIFIER_ID);
        speedAttr.removeModifier(SATURATION_SPEED_MODIFIER_ID);
        attackAttr.removeModifier(SATURATION_ATTACK_MODIFIER_ID);

        // Calculate scaling factor (0.0 to 1.0)
        float factor = (float) saturationProgress / SATURATION_STEPS;
        if (factor <= 0.0f) return;

        // Max bonuses (adjust these values as desired)
        double maxHealthBonus = 4.0;   // +2 hearts (4 HP)
        double maxSpeedBonus = 0.003;  // +3% movement speed (base 0.1 → 0.103)
        double maxAttackBonus = 0.5;   // +0.5 attack damage

        // Apply scaled modifiers
        healthAttr.addTransientModifier(new AttributeModifier(
                SATURATION_HEALTH_MODIFIER_ID,
                maxHealthBonus * factor,
                AttributeModifier.Operation.ADD_VALUE
        ));
        speedAttr.addTransientModifier(new AttributeModifier(
                SATURATION_SPEED_MODIFIER_ID,
                maxSpeedBonus * factor,
                AttributeModifier.Operation.ADD_VALUE
        ));
        attackAttr.addTransientModifier(new AttributeModifier(
                SATURATION_ATTACK_MODIFIER_ID,
                maxAttackBonus * factor,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    @Override
    public void readData(ValueInput input) {
        TheSpell.LOGGER.info("Loading Essence data from ValueInput");

        currentEssence = input.getInt("CurrentEssence").orElse(0);
        storedMicroPoints = input.getInt("StoredMicroPoints").orElse(0);
        saturationProgress = input.getInt("SaturationProgress").orElse(0);

        String rankName = input.getString("Rank").orElse("");
        try {
            rank = Ranks.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            rank = Ranks.PLAYER;
        }

        // Read nightmare seed as int (0 = false, 1 = true)
        hasNightmareSeed = input.getInt("NightmareSeed").orElse(0) != 0;

        TheSpell.LOGGER.info("Loaded: essence=" + currentEssence +
                ", micro=" + storedMicroPoints +
                ", sat=" + saturationProgress +
                ", rank=" + rank +
                ", seed=" + hasNightmareSeed);

        // Apply saturation modifiers after loading
        updateSaturationModifiers();
    }

    @Override
    public void writeData(ValueOutput output) {
        TheSpell.LOGGER.info("Saving Essence data to ValueOutput");

        output.putInt("CurrentEssence", currentEssence);
        output.putInt("StoredMicroPoints", storedMicroPoints);
        output.putInt("SaturationProgress", saturationProgress);
        output.putString("Rank", rank.name());
        output.putInt("NightmareSeed", hasNightmareSeed ? 1 : 0);

        TheSpell.LOGGER.info("Saved: essence=" + currentEssence +
                ", micro=" + storedMicroPoints +
                ", sat=" + saturationProgress +
                ", rank=" + rank +
                ", seed=" + hasNightmareSeed);
    }
}