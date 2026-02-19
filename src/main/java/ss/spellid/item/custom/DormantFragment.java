package ss.spellid.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DormantFragment extends Item {

    public DormantFragment(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        //This is the client side of things
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = user.getItemInHand(hand);
        if(!stack.isEmpty()) {
            stack.shrink(1);

            if (stack.isEmpty()) {
                user.setItemInHand(hand, ItemStack.EMPTY);
            }

            user.giveExperiencePoints(1);

        }
        return InteractionResult.SUCCESS;
    }
}
