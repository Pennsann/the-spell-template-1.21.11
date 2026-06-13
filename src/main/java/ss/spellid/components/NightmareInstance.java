package ss.spellid.components;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.ladysnake.cca.api.v3.component.Component;

public interface NightmareInstance extends Component {
    void setNightmareId(Identifier id);
    Identifier getNightmareId();
    void setCompleted(boolean completed);
    boolean isCompleted();
    void setSeedPos(BlockPos pos);
    BlockPos getSeedPos();
    void setSeedDimension(ResourceKey<Level> dimension);
    ResourceKey<Level> getSeedDimension();
}