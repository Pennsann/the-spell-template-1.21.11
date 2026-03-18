package ss.spellid.components;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.TheSpell;

public class NightmareInstanceImpl implements NightmareInstance {
    private Identifier nightmareId = null;
    private boolean completed = false;

    @Override
    public void setNightmareId(Identifier id) {
        this.nightmareId = id;
    }

    @Override
    public Identifier getNightmareId() {
        return nightmareId;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void readData(ValueInput input) {
        String idStr = input.getString("NightmareId").orElse(null);
        if (idStr != null) {
            nightmareId = Identifier.parse(idStr);
        }
        completed = input.getInt("Completed").orElse(0) != 0;
    }

    @Override
    public void writeData(ValueOutput output) {
        if (nightmareId != null) {
            output.putString("NightmareId", nightmareId.toString());
        }
        output.putInt("Completed", completed ? 1 : 0);
    }
}