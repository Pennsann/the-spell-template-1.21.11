package ss.spellid.ranks;

public enum Ranks {
    PLAYER(0, "Unawakened"),
    SLEEPER(150, "Dormant"),
    AWAKENED(400,"Awakened"),
    ASCENDED(1000, "Ascended"),
    TRANSCENDENT(2500, "Transcendent"),
    SUPREME(6000, "Supreme"),
    SACRED(15000, "Sacred"),
    DIVINE(50000, "Divine");

    private final int baseMaxEssence;
    private final String displayName;

    Ranks(int baseMaxEssence, String displayName) {
        this.baseMaxEssence = baseMaxEssence;
        this.displayName = displayName;
    }

    public int getBaseMaxEssence() {
        return baseMaxEssence;
    }

    public String getDisplayName() {
        return displayName;
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

    /**
     * Essence regen rate per second (per 20 ticks).
     * PLAYER = 0 (no regen), SLEEPER = 1, AWAKENED = 2, ASCENDED = 3, etc.
     */
    public int getEssenceRegenRate() {
        return this.ordinal(); // PLAYER ordinal 0 -> 0, SLEEPER 1 -> 1, ...
    }

    /**
     * Determines how many saturation points are gained from absorbing one fragment of the given tier.
     * Logic:
     * - rankOrdinal 1 (SLEEPER) matches fragment tier DORMANT (ordinal 0)
     * - rankOrdinal 2 (AWAKENED) matches fragment tier AWAKENED (ordinal 1)
     * - etc.
     * So the matching tier ordinal = rankOrdinal - 1.
     *
     * @param fragmentTier the tier of the absorbed fragment
     * @return saturation points gained (0.0 if cannot absorb)
     */
    public double getAbsorptionEfficiencyForFragmentTier(FragmentTier fragmentTier) {
        if (!this.hasSoulCore()) {
            return 0.0; // PLAYER cannot absorb
        }

        int rankOrd = this.ordinal();          // 1 = SLEEPER, 2 = AWAKENED, 3 = ASCENDED, ...
        int fragOrd = fragmentTier.ordinal();  // 0 = DORMANT, 1 = AWAKENED, 2 = ASCENDED, ...

        // Matching tier for a rank is rankOrd - 1
        int tierDiff = fragOrd - (rankOrd - 1);

        if (tierDiff == 0) {
            return 1.0;  // same tier -> 1 point per fragment
        } else if (tierDiff == 1) {
            return 5.0;  // one tier higher -> 5 points per fragment
        } else if (tierDiff == -1) {
            return 0.2;  // one tier lower -> 0.2 point per fragment (5 fragments = 1 point)
        } else {
            return 0.0;  // more than one tier apart -> cannot absorb
        }
    }
}