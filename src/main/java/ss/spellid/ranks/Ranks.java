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

    public int getHealPerRegenCycle() {
        return switch (this) {
            case PLAYER -> 1;         // vanilla
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
            case PLAYER -> 0;          // no regen
            case SLEEPER -> 1;         // 1 hunger point per minute (0.5 shank)
            case AWAKENED -> 2;        // 2 hunger points per minute
            case ASCENDED -> 3;
            case TRANSCENDENT -> 4;
            case SUPREME -> 5;
            case SACRED -> 6;
            case DIVINE -> 8;
        };
    }

    private int baseMaxEssence;
    private String displayName;

    Ranks(int baseMaxEssence, String displayName) {
        this.baseMaxEssence = baseMaxEssence;
        this.displayName = displayName;
    }

    Ranks(int baseMaxEssence) {
        this.baseMaxEssence = baseMaxEssence;
    }

    public int getBaseMaxEssence() {
        return baseMaxEssence;
    }
    public String getDisplayName() { return displayName; }

    public boolean hasSoulCore(){
        return this != PLAYER;
    }

    public double getAbsorptionEfficiencyForFragmentTier(FragmentTier fragmentTier){
        if(!this.hasSoulCore()){
            return 0.0; // cannot absorb
        }

        int thisOrdinal = this.ordinal();
        int tierOrdinal = fragmentTier.ordinal();
        int tierDifference = tierOrdinal - thisOrdinal;

        // If the fragment is too low or too high rank, cannot absorb
        if (tierDifference < -1 || tierDifference > 1) {
            return 0.0;
        }

        double efficiency;

        // For SLEEPER (ordinal 1)
        if (this == SLEEPER) {
            if (fragmentTier == FragmentTier.DORMANT) { // DORMANT is ordinal 0, difference = -1
                efficiency = 1.0; // Sleeper gets 1 point from Dormant fragment
            } else if (fragmentTier == FragmentTier.AWAKENED) { // AWAKENED is ordinal 1, difference = 0
                efficiency = 5.0; // Sleeper gets 5 points from Awakened fragment
            } else {
                efficiency = 0.0;
            }
        }
        // For AWAKENED (ordinal 2)
        else if (this == AWAKENED) {
            if (fragmentTier == FragmentTier.DORMANT) { // DORMANT is ordinal 0, difference = -2
                efficiency = 0.0; // Too low, cannot absorb
            } else if (fragmentTier == FragmentTier.AWAKENED) { // AWAKENED is ordinal 1, difference = -1
                efficiency = 0.2; // Awakened needs 5 Dormant fragments for 1 point
            } else if (fragmentTier == FragmentTier.ASCENDED) { // ASCENDANT is ordinal 2, difference = 0
                efficiency = 1.0; // Awakened gets 1 point from Ascendant fragment
            } else {
                efficiency = 0.0;
            }
        }
        // For future ranks, use the formula
        else {
            efficiency = switch (tierDifference){
                case -1 -> 0.2; // 1 tier lower -> needs 5 frags for +1 sat
                case 0 -> 1.0;  // same tier = 1 point
                case 1 -> 5.0;  // 1 tier higher = 5 points
                default -> 0.0;
            };
        }

        return efficiency;
    }
}