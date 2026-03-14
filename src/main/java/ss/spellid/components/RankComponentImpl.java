package ss.spellid.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.TheSpell;
import ss.spellid.ranks.Ranks;

public class RankComponentImpl implements RankComponent {
    private Ranks rank = Ranks.PLAYER;
    private final Player player;

    public RankComponentImpl(Player player) {
        this.player = player;
        // any init that needs the player
    }

    @Override
    public Ranks getRank() {
        return rank;
    }

    @Override
    public void setRank(Ranks newRank) {
        this.rank = (newRank != null) ? newRank : Ranks.PLAYER;
    }

    @Override
    public void readData(ValueInput input) {
        TheSpell.LOGGER.info("Loading Rank data from ValueInput");

        String rankName = input.getString("Rank").orElse("");
        try {
            rank = Ranks.valueOf(rankName);
            TheSpell.LOGGER.info("Loaded rank: " + rank);
        } catch (IllegalArgumentException e) {
            rank = Ranks.PLAYER;
            TheSpell.LOGGER.info("Invalid rank name, defaulting to PLAYER");
        }
    }

    @Override
    public void writeData(ValueOutput output) {
        TheSpell.LOGGER.info("Saving Rank to ValueOutput: " + rank);
        output.putString("Rank", rank.name());
    }
}
