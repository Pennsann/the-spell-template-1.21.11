package ss.spellid.ranks;

import ss.spellid.TheSpell;

public enum Ranks {
    PLAYER(0),
    SLEEPER(150),
    AWAKENED(400),
    //add more and more ranks when satisfied :D
    ;

    private int baseMaxEssence;

    Ranks(int baseMaxEssence) {
        this.baseMaxEssence = baseMaxEssence;
    }

    public int getBaseMaxEssence() {
        return baseMaxEssence;
    }

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

        TheSpell.LOGGER.info("Rank: " + this + " (ordinal: " + thisOrdinal +
                "), Fragment: " + fragmentTier + " (ordinal: " + tierOrdinal +
                "), Difference: " + tierDifference);

        // If the fragment is too low or too high rank, cannot absorb
        if (tierDifference < -1 || tierDifference > 1) {
            TheSpell.LOGGER.info("Tier difference too great, cannot absorb");
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
            } else if (fragmentTier == FragmentTier.ASCENDANT) { // ASCENDANT is ordinal 2, difference = 0
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

        TheSpell.LOGGER.info("Absorption efficiency: " + efficiency);
        return efficiency;
    }
}