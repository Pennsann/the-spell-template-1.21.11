package ss.spellid.components;

import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public interface EssenceComponent extends Component {
    int getCurrentEssence();
    void setCurrentEssence(int value);
    void addCurrentEssence(int amount);

    int getMaxEssence();

    int getSaturationProgress();
    int getSaturationMax();

    void absorbFragment(FragmentTier fragment);

    Ranks getRank();
    void setRank(Ranks newRank);

}