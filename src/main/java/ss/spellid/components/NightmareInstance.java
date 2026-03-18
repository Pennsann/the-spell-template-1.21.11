package ss.spellid.components;

import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.Component;

public interface NightmareInstance extends Component {
    void setNightmareId(Identifier id);
    Identifier getNightmareId();
    void setCompleted(boolean completed);
    boolean isCompleted();
}