package ss.spellid.ranks;

public enum Ranks {
    PLAYER(0, "Unawakened", 0.0),
    SLEEPER(150, "Dormant", 0.05),
    AWAKENED(400, "Awakened", 0.10),
    ASCENDED(1000, "Ascended", 0.15),
    TRANSCENDENT(2500, "Transcendent", 0.20),
    SUPREME(6000, "Supreme", 0.25),
    SACRED(15000, "Sacred", 0.30),
    DIVINE(50000, "Divine", 0.40);

    private final int baseMaxEssence;
    private final String displayName;
    private final double maxSaturationBonus; // decimal, e.g., 0.05 = 5%

    Ranks(int baseMaxEssence, String displayName, double maxSaturationBonus) {
        this.baseMaxEssence = baseMaxEssence;
        this.displayName = displayName;
        this.maxSaturationBonus = maxSaturationBonus;
    }

    public int getBaseMaxEssence() {
        return baseMaxEssence;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMaxSaturationBonus() {
        return maxSaturationBonus;
    }

    public boolean hasSoulCore() {
        return this != PLAYER;
    }

    public int getHealPerRegenCycle() {
        return switch (this) {
            case PLAYER -> 1;
            case SLEEPER -> 2;
            case AWAKENED -> 3;
            case ASCENDED -> 4;
            case TRANSCENDENT -> 5;
            case SUPREME -> 6;
            case SACRED -> 7;
            case DIVINE -> 8;
        };
    }

    public int getHungerRegenPerMinute() {
        return switch (this) {
            case PLAYER -> 0;
            case SLEEPER -> 1;
            case AWAKENED -> 2;
            case ASCENDED -> 3;
            case TRANSCENDENT -> 4;
            case SUPREME -> 5;
            case SACRED -> 6;
            case DIVINE -> 8;
        };
    }

    public int getEssenceRegenRate() {
        return this.ordinal(); // PLAYER 0, SLEEPER 1, AWAKENED 2, ...
    }

    public double getAbsorptionEfficiencyForFragmentTier(FragmentTier fragmentTier) {
        if (!this.hasSoulCore()) {
            return 0.0;
        }

        int rankOrd = this.ordinal();
        int fragOrd = fragmentTier.ordinal();
        int tierDiff = fragOrd - (rankOrd - 1);

        if (tierDiff == 0) {
            return 1.0;
        } else if (tierDiff == 1) {
            return 5.0;
        } else if (tierDiff == -1) {
            return 0.2;
        } else {
            return 0.0;
        }
    }
}