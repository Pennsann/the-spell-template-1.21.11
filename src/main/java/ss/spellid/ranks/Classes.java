package ss.spellid.ranks;

public enum Classes {
    BEAST(1.0),    // multiplier for essence capacity? or flat bonus?
    MONSTER(1.2),
    DEMON(1.5),
    DEVIL(2.0),
    TYRANT(2.5),
    TITAN(3.0),
    GOD(5.0);

    private final double essenceMultiplier;

    Classes(double essenceMultiplier) {
        this.essenceMultiplier = essenceMultiplier;
    }

    public double getEssenceMultiplier() { return essenceMultiplier; }
}