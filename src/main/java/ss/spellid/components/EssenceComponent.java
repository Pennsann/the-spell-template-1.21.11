package ss.spellid.components;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public interface EssenceComponent extends Component {
    int getCurrentEssence();
    void setCurrentEssence(int value);
    void addCurrentEssence(int amount);

    int getMaxEssence();

    void updateSaturationModifiers();

    int getSaturationProgress();
    int getSaturationMax();

    void absorbFragment(FragmentTier fragment);

    Ranks getRank();
    void setRank(Ranks newRank);

    // Nightmare seed flag
    boolean hasNightmareSeed();
    void setNightmareSeed(boolean hasSeed);
}