package ss.spellid.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;
import ss.spellid.ranks.Ranks;

public interface RankComponent extends Component {
    Ranks getRank();
    void setRank(Ranks newRank);
    void readFromNbt(CompoundTag tag);
    void wrtieNbt(CompoundTag tag);
    void readData(ValueInput input);
    void writeData(ValueOutput output);
}
