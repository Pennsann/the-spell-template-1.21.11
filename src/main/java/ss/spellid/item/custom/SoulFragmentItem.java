package ss.spellid.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import org.w3c.dom.Text;
import ss.spellid.ModComponents;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.RankComponent;
import ss.spellid.ranks.FragmentTier;
import ss.spellid.ranks.Ranks;

public abstract class SoulFragmentItem extends Item {
    public SoulFragmentItem(Settings settings) {
        super(new Properties().stacksTo(16));
    }

    public abstract FragmentTier getTier();

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()){
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = user.getItemInHand(hand);

        RankComponent rankComp = ModComponents.RANK.get(user);
        if(!rankComp.getRank().hasSoulCore()){
            user.displayClientMessage(Component.literal("Your too weak to break this fragment..."), false);
            return InteractionResult.FAIL;
        }

        EssenceComponent essenceComp = ModComponents.ESSENCE.get(user);
        essenceComp.absorbFragment(getTier());

        int progress = essenceComp.getSaturationProgress();
        user.displayClientMessage(Component.literal("Fragment absorbed. Saturation:" + progress + "/" + essenceComp.getSaturationMax()), false );

        stack.shrink(1);
        if (stack.isEmpty()) {
            user.setItemInHand(hand, ItemStack.EMPTY);
        }

        return InteractionResult.SUCCESS;
    }
}
