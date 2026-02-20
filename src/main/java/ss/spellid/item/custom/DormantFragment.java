package ss.spellid.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ss.spellid.ranks.FragmentTier;

public class DormantFragment extends SoulFragmentItem {

    public DormantFragment(Properties properties) {
        super(properties);
    }

    @Override
    public FragmentTier getTier() {
        return FragmentTier.DORMANT;
    }
}
