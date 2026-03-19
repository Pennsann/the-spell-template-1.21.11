package ss.spellid.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;
import ss.spellid.aspect.Aspect;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public interface EssenceComponent extends Component {

    int getCurrentEssence();
    void setCurrentEssence(int value);
    void addCurrentEssence(int amount);
    int getMaxEssence();
    int getSaturationProgress();
    void setSaturationProgress(int value);
    int getSaturationMax();
    void absorbFragment(FragmentTier fragment);
    Ranks getRank();
    void setRank(Ranks newRank);
    boolean hasNightmareSeed();
    void setNightmareSeed(boolean hasSeed);
    void setAspectId(String aspectId);
    String getAspectId();
    void tickRegen();
    void updateSaturationModifiers();

    // Anchor methods
    boolean hasAnchor();
    void setAnchor(int x, int y, int z);
    int getAnchorX();
    int getAnchorY();
    int getAnchorZ();
    void clearAnchor();

    // Sleeper timer methods
    long getSleeperStartTime();
    void setSleeperStartTime(long time);
    boolean isSentToDreamRealm();
    void setSentToDreamRealm(boolean sent);

    long getLastAbilityUseTime();
    void setLastAbilityUseTime(long time);
}