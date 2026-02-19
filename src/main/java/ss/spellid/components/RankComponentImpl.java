package ss.spellid.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.ranks.Ranks;

public class RankComponentImpl implements RankComponent {
    private Ranks rank = Ranks.PLAYER;

    @Override
    public Ranks getRank() {
        return rank;
    }

    @Override
    public void setRank(Ranks newRank) {
        this.rank = (newRank != null) ? newRank : Ranks.PLAYER;
    }

    @Override
    public  void readFromNbt(CompoundTag tag){
        String rankName = tag.getString("Rank").orElse("");
        try{
            rank = Ranks.valueOf(rankName);
        } catch (IllegalArgumentException e){
            rank = Ranks.PLAYER;
        }
    }

    @Override
    public void wrtieNbt(CompoundTag tag){
        tag.putString("Rank", rank.toString());
    }

    @Override
    public void readData(ValueInput input) {}

    @Override
    public void writeData(ValueOutput output) {}
}
