package ss.spellid.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.block.ModBlockEntities;

public class NightmareSeedBlockEntity extends BlockEntity {
    private String nightmareId = null;

    public NightmareSeedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIGHTMARE_SEED_BE, pos, state);
    }

    public String getNightmareId() { return nightmareId; }
    public void setNightmareId(String id) { this.nightmareId = id; setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (nightmareId != null) {
            output.putString("nightmare_id", nightmareId);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        nightmareId = input.getString("nightmare_id").orElse(null);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}