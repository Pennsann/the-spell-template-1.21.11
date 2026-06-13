package ss.spellid.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NightmareInstanceImpl implements NightmareInstance {
    private Identifier nightmareId = null;
    private boolean completed = false;
    private BlockPos seedPos = null;
    private ResourceKey<Level> seedDimension = null;

    @Override
    public void setNightmareId(Identifier id) { this.nightmareId = id; }
    @Override
    public Identifier getNightmareId() { return nightmareId; }
    @Override
    public void setCompleted(boolean completed) { this.completed = completed; }
    @Override
    public boolean isCompleted() { return completed; }
    @Override
    public void setSeedPos(BlockPos pos) { this.seedPos = pos; }
    @Override
    public BlockPos getSeedPos() { return seedPos; }
    @Override
    public void setSeedDimension(ResourceKey<Level> dimension) { this.seedDimension = dimension; }
    @Override
    public ResourceKey<Level> getSeedDimension() { return seedDimension; }

    @Override
    public void writeData(ValueOutput output) {
        if (nightmareId != null) {
            output.putString("NightmareId", nightmareId.toString());
        }
        output.putInt("Completed", completed ? 1 : 0);
        if (seedPos != null) {
            output.putInt("SeedX", seedPos.getX());
            output.putInt("SeedY", seedPos.getY());
            output.putInt("SeedZ", seedPos.getZ());
        }
        if (seedDimension != null) {
            output.putString("SeedDimension", seedDimension.identifier().toString());
        }
    }

    @Override
    public void readData(ValueInput input) {
        String idStr = input.getString("NightmareId").orElse(null);
        if (idStr != null) {
            nightmareId = Identifier.parse(idStr);
        }
        completed = input.getInt("Completed").orElse(0) != 0;
        int x = input.getInt("SeedX").orElse(0);
        int y = input.getInt("SeedY").orElse(0);
        int z = input.getInt("SeedZ").orElse(0);
        if (x != 0 || y != 0 || z != 0) {
            seedPos = new BlockPos(x, y, z);
        }
        String dimStr = input.getString("SeedDimension").orElse(null);
        if (dimStr != null) {
            seedDimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimStr));
        }
    }
}