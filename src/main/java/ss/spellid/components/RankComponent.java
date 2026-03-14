package ss.spellid.components;

import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import ss.spellid.ranks.Ranks;

public interface RankComponent extends Component {
    Ranks getRank();
    void setRank(Ranks newRank);

    default void copyFrom(RankComponent other) {
        this.setRank(other.getRank());
    }
}