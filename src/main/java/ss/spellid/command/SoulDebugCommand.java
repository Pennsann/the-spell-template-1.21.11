package ss.spellid.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import ss.spellid.components.RankComponentInitializer;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;

public class SoulDebugCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soul_debug")
                .executes(context -> {
                    Player player = context.getSource().getPlayerOrException();
                    var rankComp = RANK_KEY.get(player);
                    var essenceComp = RankComponentInitializer.ESSENCE.get(player);

                    player.displayClientMessage(Component.literal("§6Rank: §f" + rankComp.getRank().getDisplayName()), false);
                    player.displayClientMessage(Component.literal("§6Essence: §f" + essenceComp.getCurrentEssence() + " / " + essenceComp.getMaxEssence()), false);
                    player.displayClientMessage(Component.literal("§6Saturation: §f" + essenceComp.getSaturationProgress() + "/" + essenceComp.getSaturationMax()), false);
                    return 1;
                })
        );
    }
}