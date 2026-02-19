package ss.spellid.ranks;

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
        return this !=PLAYER;
    }
    public double getAbsorptionEfficiencyForFragmentTier(Ranks fragmentRank){
        if(!this.hasSoulCore()){
            return 0.0; // can not absorb
        }
        int tierDifference = fragmentRank.ordinal() - this.ordinal();
        if(tierDifference < -1 || tierDifference > 1) {
            return 0.0; // can not absorb
        }

        return switch (tierDifference){
            case -1 -> 0.2; //1 tier lower -> needs 5 frags for +1 sat
            case 0 -> 1.0; //same tier = 1 point
            case 1 -> 5.0; //1 tier higher +5
            default -> 0.0;
        };
    };
}
