package ss.spellid.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import ss.spellid.block.ModBlocks;
import ss.spellid.block.entity.NightmareSeedBlockEntity;

public class NightmareSeedItem extends BlockItem {
    public NightmareSeedItem(Properties properties) {
        super(ModBlocks.NIGHTMARE_SEED, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof NightmareSeedBlockEntity seedBe) {
                ItemStack stack = context.getItemInHand();
                var data = stack.get(DataComponents.CUSTOM_DATA);
                if (data != null) {
                    CompoundTag tag = data.copyTag();
                    if (tag.contains("nightmare_id")) {
                        String nightmareId = tag.getString("nightmare_id").orElse(null);
                        if (nightmareId != null) {
                            seedBe.setNightmareId(nightmareId);
                        }
                    }
                }
            }
        }
        return result;
    }
}