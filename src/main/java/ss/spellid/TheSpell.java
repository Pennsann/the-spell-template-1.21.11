package ss.spellid;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ss.spellid.components.RankComponentImpl;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.item.ModItems;
import ss.spellid.ranks.Ranks;
import ss.spellid.components.RankComponent;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;

public class TheSpell implements ModInitializer {
	public static final String MOD_ID = "the-spell";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info(MOD_ID + " initialized");
		ModItems.init();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			try{
				Player player = handler.player;
				var rankComp = RANK_KEY.get(player);

				if(rankComp.getRank() == Ranks.PLAYER){
					rankComp.setRank(Ranks.SLEEPER);
					player.displayClientMessage(Component.literal("§e[Dev] Your rank has been set to SLEEPER")
							, false);
				}
				else{
					player.displayClientMessage(Component.literal("§e[Dev] Your rank is: " + rankComp.getRank())
							, false);
				}
			}catch (Exception e){
				TheSpell.LOGGER.error("Error in player join event", e);
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("soul_debug")
					.executes(context -> {
						Player player = context.getSource().getPlayerOrException();
						var rankComp = RANK_KEY.get(player);
						var essenceComp = RankComponentInitializer.ESSENCE.get(player);

						player.displayClientMessage(Component.literal("§6Rank: §f" + rankComp.getRank().name()), false);
						player.displayClientMessage(Component.literal("§6Essence: §f" + essenceComp.getCurrentEssence() + " / " + essenceComp.getMaxEssence()), false);
						player.displayClientMessage(Component.literal("§6Saturation: §f" + essenceComp.getSaturationProgress() + "/" + essenceComp.getSaturationMax()), false);
						return 1;
					}));
		});
	}
}