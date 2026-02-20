package ss.spellid;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ss.spellid.item.ModItems;
import ss.spellid.ranks.Ranks;

public class TheSpell implements ModInitializer {
	public static final String MOD_ID = "the-spell";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info(MOD_ID + " initialized");
		ModItems.init();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			Player player = handler.player;
			var rankComp = ModComponents.RANK.get(player);
			rankComp.setRank(Ranks.SLEEPER);
			player.displayClientMessage(Component.literal("§e[Dev] Your rank has been set to SLEEPER"), false);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("soul_debug")
					.executes(context -> {
						Player player = context.getSource().getPlayerOrException();
						var rankComp = ModComponents.RANK.get(player);
						var essenceComp = ModComponents.ESSENCE.get(player);

						player.displayClientMessage(Component.literal("§6Rank: §f" + rankComp.getRank().name()), false);
						player.displayClientMessage(Component.literal("§6Essence: §f" + essenceComp.getCurrentEssence() + " / " + essenceComp.getMaxEssence()), false);
						player.displayClientMessage(Component.literal("§6Saturation: §f" + essenceComp.getSaturationProgress() + "/" + essenceComp.getSaturationMax()), false);
						return 1;
					}));
		});
	}
}