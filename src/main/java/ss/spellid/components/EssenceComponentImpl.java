package ss.spellid.components;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.TheSpell;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.Aspects;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public class EssenceComponentImpl implements EssenceComponent {
    private static final int SATURATION_STEPS = 1000;
    private static final float TARGET_BONUS = 0.5f;
    private static final int MICRO_PER_POINT = 100;

    private static final Identifier SATURATION_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "saturation_health");
    private static final Identifier SATURATION_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "saturation_speed");
    private static final Identifier SATURATION_ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "saturation_attack");

    private int currentEssence = 0;
    private int storedMicroPoints = 0;
    private int saturationProgress = 0;
    private Ranks rank = Ranks.PLAYER;
    private boolean hasNightmareSeed = false;
    private String aspectId = null;
    private int regenTimer = 0;
    private long sleeperStartTime = 0;
    private boolean sentToDreamRealm = false;

    // Anchor fields
    private int anchorX = 0;
    private int anchorY = 0;
    private int anchorZ = 0;
    private boolean hasAnchor = false;

    private long lastAbilityUseTime = 0;

    private final Entity entity;

    public EssenceComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public int getCurrentEssence() { return currentEssence; }

    @Override
    public void setCurrentEssence(int value) {
        currentEssence = Math.max(0, Math.min(value, getMaxEssence()));
    }

    @Override
    public void addCurrentEssence(int amount) { setCurrentEssence(getCurrentEssence() + amount); }

    @Override
    public int getMaxEssence() {
        Ranks rank = RankComponentInitializer.RANK_KEY.get(entity).getRank();
        if (!rank.hasSoulCore()) return 0;
        int base = rank.getBaseMaxEssence();
        float bonus = TARGET_BONUS * (saturationProgress / (float) SATURATION_STEPS);
        return (int) (base * (1 + bonus));
    }

    @Override
    public int getSaturationProgress() { return saturationProgress; }

    @Override
    public int getSaturationMax() { return SATURATION_STEPS; }

    @Override
    public void setSaturationProgress(int value) {
        this.saturationProgress = Math.max(0, Math.min(value, SATURATION_STEPS));
        updateSaturationModifiers();
    }

    @Override
    public void absorbFragment(FragmentTier fragment) {
        if (saturationProgress >= SATURATION_STEPS) {
            return;
        }

        Ranks currentRank = RankComponentInitializer.RANK_KEY.get(entity).getRank();
        double pointsGained = currentRank.getAbsorptionEfficiencyForFragmentTier(fragment);

        if (pointsGained <= 0) {
            return;
        }

        if (pointsGained == 1.0 || pointsGained == 5.0) {
            saturationProgress += (int) pointsGained;
        } else {
            int microToAdd = (int) (pointsGained * MICRO_PER_POINT);
            storedMicroPoints += microToAdd;

            int newPoints = storedMicroPoints / MICRO_PER_POINT;
            storedMicroPoints %= MICRO_PER_POINT;

            if (newPoints > 0) {
                saturationProgress += newPoints;
            }
        }

        if (saturationProgress > SATURATION_STEPS) saturationProgress = SATURATION_STEPS;

        updateSaturationModifiers();
    }

    @Override
    public Ranks getRank() { return rank; }

    @Override
    public void setRank(Ranks newRank) { this.rank = newRank != null ? newRank : Ranks.PLAYER; }

    @Override
    public boolean hasNightmareSeed() { return hasNightmareSeed; }

    @Override
    public void setNightmareSeed(boolean hasSeed) { this.hasNightmareSeed = hasSeed; }

    @Override
    public String getAspectId() { return aspectId; }

    @Override
    public void setAspectId(String newAspectId) {
        if (aspectId != null && entity instanceof Player player) {
            Aspect oldAspect = Aspects.get(Identifier.parse(aspectId));
            if (oldAspect != null) oldAspect.removeFrom(player);
        }
        this.aspectId = newAspectId;
        if (newAspectId != null && entity instanceof Player player) {
            Aspect newAspect = Aspects.get(Identifier.parse(newAspectId));
            if (newAspect != null) newAspect.applyTo(player);
        }
    }

    public void applyAspectToPlayer() {
        if (aspectId != null && entity instanceof Player player) {
            Aspect aspect = Aspects.get(Identifier.parse(aspectId));
            if (aspect != null) aspect.applyTo(player);
        }
    }

    @Override
    public void updateSaturationModifiers() {
        if (!(entity instanceof Player player)) return;
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (healthAttr == null || speedAttr == null || attackAttr == null) return;

        // Remove old saturation modifiers
        healthAttr.removeModifier(SATURATION_HEALTH_MODIFIER_ID);
        speedAttr.removeModifier(SATURATION_SPEED_MODIFIER_ID);
        attackAttr.removeModifier(SATURATION_ATTACK_MODIFIER_ID);

        if (saturationProgress <= 0) return;

        // Get rank's max saturation bonus percentage
        Ranks rank = RankComponentInitializer.RANK_KEY.get(player).getRank();
        double maxPercent = rank.getMaxSaturationBonus();
        if (maxPercent <= 0.0) return;

        // Compute factor (linear with saturation)
        double factor = (saturationProgress / (double) SATURATION_STEPS) * maxPercent;

        // Apply as ADD_MULTIPLIED_TOTAL (multiplies the total value after all additions)
        healthAttr.addTransientModifier(new AttributeModifier(
                SATURATION_HEALTH_MODIFIER_ID, factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        speedAttr.addTransientModifier(new AttributeModifier(
                SATURATION_SPEED_MODIFIER_ID, factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        attackAttr.addTransientModifier(new AttributeModifier(
                SATURATION_ATTACK_MODIFIER_ID, factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void tickRegen() {
        if (++regenTimer >= 20) {
            regenTimer = 0;
            Ranks rank = RankComponentInitializer.RANK_KEY.get(entity).getRank();
            int rate = rank.getEssenceRegenRate();
            if (rate > 0 && currentEssence < getMaxEssence()) {
                currentEssence = Math.min(currentEssence + rate, getMaxEssence());
            }
        }
    }

    @Override
    public long getSleeperStartTime() { return sleeperStartTime; }

    @Override
    public void setSleeperStartTime(long time) { this.sleeperStartTime = time; }

    @Override
    public boolean isSentToDreamRealm() { return sentToDreamRealm; }

    @Override
    public void setSentToDreamRealm(boolean sent) { this.sentToDreamRealm = sent; }

    // Anchor methods
    @Override
    public boolean hasAnchor() { return hasAnchor; }

    @Override
    public void setAnchor(int x, int y, int z) {
        this.anchorX = x;
        this.anchorY = y;
        this.anchorZ = z;
        this.hasAnchor = true;
    }

    @Override
    public int getAnchorX() { return anchorX; }

    @Override
    public int getAnchorY() { return anchorY; }

    @Override
    public int getAnchorZ() { return anchorZ; }

    @Override
    public void clearAnchor() {
        this.hasAnchor = false;
        this.anchorX = 0;
        this.anchorY = 0;
        this.anchorZ = 0;
    }

    @Override
    public long getLastAbilityUseTime() { return lastAbilityUseTime; }

    @Override
    public void setLastAbilityUseTime(long time) { this.lastAbilityUseTime = time; }

    @Override
    public void writeData(ValueOutput output) {
        output.putInt("CurrentEssence", currentEssence);
        output.putInt("StoredMicroPoints", storedMicroPoints);
        output.putInt("SaturationProgress", saturationProgress);
        output.putString("Rank", rank.name());
        output.putInt("NightmareSeed", hasNightmareSeed ? 1 : 0);
        if (aspectId != null) output.putString("AspectId", aspectId);
        output.putLong("SleeperStartTime", sleeperStartTime);
        output.putInt("SentToDreamRealm", sentToDreamRealm ? 1 : 0);
        // Anchor
        output.putInt("AnchorX", anchorX);
        output.putInt("AnchorY", anchorY);
        output.putInt("AnchorZ", anchorZ);
        output.putInt("HasAnchor", hasAnchor ? 1 : 0);
        output.putLong("LastAbilityUseTime", lastAbilityUseTime);
    }

    @Override
    public void readData(ValueInput input) {
        currentEssence = input.getInt("CurrentEssence").orElse(0);
        storedMicroPoints = input.getInt("StoredMicroPoints").orElse(0);
        saturationProgress = input.getInt("SaturationProgress").orElse(0);

        String rankName = input.getString("Rank").orElse("");
        try {
            rank = Ranks.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            rank = Ranks.PLAYER;
        }

        hasNightmareSeed = input.getInt("NightmareSeed").orElse(0) != 0;
        aspectId = input.getString("AspectId").orElse(null);
        sleeperStartTime = input.getLong("SleeperStartTime").orElse(0L);
        sentToDreamRealm = input.getInt("SentToDreamRealm").orElse(0) != 0;

        // Anchor
        anchorX = input.getInt("AnchorX").orElse(0);
        anchorY = input.getInt("AnchorY").orElse(0);
        anchorZ = input.getInt("AnchorZ").orElse(0);
        hasAnchor = input.getInt("HasAnchor").orElse(0) != 0;
        lastAbilityUseTime = input.getLong("LastAbilityUseTime").orElse(0L);

        updateSaturationModifiers();
        applyAspectToPlayer();
    }
}