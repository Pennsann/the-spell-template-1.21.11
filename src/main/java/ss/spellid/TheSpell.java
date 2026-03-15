package ss.spellid;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.Aspects;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.effect.ModEffects;
import ss.spellid.event.NightmareCompletionHandler;
import ss.spellid.event.SleepHandler;
import ss.spellid.item.ModItems;
import ss.spellid.ranks.Ranks;

import java.util.Set;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class TheSpell implements ModInitializer {
	public static final String MOD_ID = "the-spell";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info(MOD_ID + " initialized");
		ModItems.init();
		ModEffects.register();

		// Join event: display current rank
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			try {
				Player player = handler.player;
				var rankComp = RANK_KEY.get(player);
				player.displayClientMessage(Component.literal("§e[Spell] Your current rank: " + rankComp.getRank().getDisplayName()), false);
				LOGGER.info("Player {} joined with rank {}", player.getName().getString(), rankComp.getRank());
			} catch (Exception e) {
				LOGGER.error("Error in player join event", e);
			}
		});

		// Register event handlers
		SleepHandler.register();
		NightmareCompletionHandler.register();
		Aspects.init(); // load aspect registry

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			// Soul debug command
			dispatcher.register(Commands.literal("soul_debug")
					.executes(context -> {
						Player player = context.getSource().getPlayerOrException();
						var rankComp = RANK_KEY.get(player);
						var essenceComp = ESSENCE.get(player);

						String aspectDisplay = "None";
						if (essenceComp.getAspectId() != null) {
							Aspect aspect = Aspects.get(Identifier.parse(essenceComp.getAspectId()));
							if (aspect != null) {
								aspectDisplay = aspect.getDisplayName().getString();
							}
						}

						player.displayClientMessage(Component.literal("§6Rank: §f" + rankComp.getRank().getDisplayName()), false);
						player.displayClientMessage(Component.literal("§6Essence: §f" + essenceComp.getCurrentEssence() + " / " + essenceComp.getMaxEssence()), false);
						player.displayClientMessage(Component.literal("§6Saturation: §f" + essenceComp.getSaturationProgress() + "/" + essenceComp.getSaturationMax()), false);
						player.displayClientMessage(Component.literal("§6Aspect: §f" + aspectDisplay), false);
						return 1;
					}));

			// Nightmare exit command (with rank-up)
			dispatcher.register(Commands.literal("nightmare_exit")
					.executes(context -> {
						Player player = context.getSource().getPlayerOrException();

						Identifier dimensionId = Identifier.fromNamespaceAndPath(MOD_ID, "first_nightmare");
						ResourceKey<Level> nightmareKey = ResourceKey.create(
								Registries.DIMENSION,
								dimensionId
						);

						if (player.level().dimension().equals(nightmareKey)) {
							ServerPlayer serverPlayer = (ServerPlayer) player;
							ServerLevel overworld = serverPlayer.level().getServer().overworld();
							double x = overworld.getRespawnData().pos().getX();
							double y = overworld.getRespawnData().pos().getY();
							double z = overworld.getRespawnData().pos().getZ();

							serverPlayer.teleportTo(
									overworld,
									x, y, z,
									Set.of(),
									player.getYRot(),
									player.getXRot(),
									false
							);
							player.displayClientMessage(Component.literal("§aYou escape the nightmare... for now."), false);

							// Promote to SLEEPER if still PLAYER (in case they didn't complete via block)
							var rankComp = RANK_KEY.get(serverPlayer);
							if (rankComp.getRank() == Ranks.PLAYER) {
								rankComp.setRank(Ranks.SLEEPER);
								player.displayClientMessage(Component.literal("§aYou have survived the First Nightmare! You are now a Sleeper."), false);
							}
						} else {
							player.displayClientMessage(Component.literal("§cYou are not in a nightmare!"), false);
						}
						return 1;
					}));

			// Spell seed commands
			dispatcher.register(Commands.literal("spell")
					.then(Commands.literal("seed")
							.then(Commands.literal("give")
									.executes(context -> {
										Player player = context.getSource().getPlayerOrException();
										var essence = ESSENCE.get(player);
										essence.setNightmareSeed(true);
										if (player instanceof ServerPlayer serverPlayer) {
											serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
													ModEffects.NIGHTMARE_SEED,
													-1,
													0,
													false,
													true,
													true
											));
										}
										player.displayClientMessage(Component.literal("§5You feel a strange seed taking root in your soul..."), false);
										LOGGER.info("Seed manually given to " + player.getName().getString());
										return 1;
									}))
							.then(Commands.literal("remove")
									.executes(context -> {
										Player player = context.getSource().getPlayerOrException();
										var essence = ESSENCE.get(player);
										essence.setNightmareSeed(false);
										player.removeEffect(ModEffects.NIGHTMARE_SEED);
										player.displayClientMessage(Component.literal("§aThe nightmare seed has been purged."), false);
										LOGGER.info("Seed manually removed from " + player.getName().getString());
										return 1;
									}))));

			// Aspect commands
			dispatcher.register(Commands.literal("spell")
					.then(Commands.literal("aspect")
							.then(Commands.literal("get")
									.executes(context -> {
										Player player = context.getSource().getPlayerOrException();
										var essence = ESSENCE.get(player);
										String aspectId = essence.getAspectId();
										if (aspectId == null) {
											player.displayClientMessage(Component.literal("§cYou have no aspect."), false);
										} else {
											Aspect aspect = Aspects.get(Identifier.parse(aspectId));
											if (aspect != null) {
												player.displayClientMessage(Component.literal("§6Your aspect: §f" + aspect.getDisplayName().getString()), false);
											} else {
												player.displayClientMessage(Component.literal("§cAspect data corrupted."), false);
											}
										}
										return 1;
									}))
							.then(Commands.literal("set")
									.then(Commands.argument("id", StringArgumentType.string())
											.executes(context -> {
												String id = context.getArgument("id", String.class);
												Player player = context.getSource().getPlayerOrException();
												Identifier aspectId;
												try {
													aspectId = Identifier.parse(id);
												} catch (Exception e) {
													player.displayClientMessage(Component.literal("§cInvalid aspect ID format. Use namespace:path (e.g., the-spell:survivor)"), false);
													return 0;
												}
												Aspect aspect = Aspects.get(aspectId);
												if (aspect == null) {
													player.displayClientMessage(Component.literal("§cAspect not found: " + id), false);
													return 0;
												}
												var essence = ESSENCE.get(player);
												essence.setAspectId(aspectId.toString());
												player.displayClientMessage(Component.literal("§aAspect set to " + aspect.getDisplayName().getString()), false);
												LOGGER.info("Aspect set for {}: {}", player.getName().getString(), aspectId);
												return 1;
											})))));
		});
	}
}