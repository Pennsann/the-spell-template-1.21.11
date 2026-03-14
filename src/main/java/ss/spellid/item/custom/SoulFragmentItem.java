package ss.spellid.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.RankComponent;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.ranks.FragmentTier;

public abstract class SoulFragmentItem extends Item {
    public SoulFragmentItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    public abstract FragmentTier getTier();

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = user.getItemInHand(hand);

        RankComponent rankComp = RankComponentInitializer.RANK_KEY.get(user);
        if (!rankComp.getRank().hasSoulCore()) {
            user.displayClientMessage(Component.literal("§cYou're too weak to break this fragment..."), false);
            return InteractionResult.FAIL;
        }

        EssenceComponent essenceComp = RankComponentInitializer.ESSENCE.get(user);

        if(essenceComp.getSaturationProgress() >= essenceComp.getSaturationMax()){
            user.displayClientMessage(Component.literal
                    ("§cYour soul core is already fully saturated!"), false);
            return InteractionResult.FAIL;
        }

        int beforeProgress = essenceComp.getSaturationProgress();

        essenceComp.absorbFragment(getTier());

        int afterProgress = essenceComp.getSaturationProgress();
        int maxSaturation = essenceComp.getSaturationMax();
        int gained = afterProgress - beforeProgress;

        String message = String.format("§aFragment absorbed! §7Saturation: §e%d§7/§e%d §8(§7+%d§8)",
                afterProgress, maxSaturation, gained);

        user.displayClientMessage(Component.literal(message), false);

        stack.shrink(1);
        if (stack.isEmpty()) {
            user.setItemInHand(hand, ItemStack.EMPTY);
        }

        return InteractionResult.SUCCESS;
    }
}