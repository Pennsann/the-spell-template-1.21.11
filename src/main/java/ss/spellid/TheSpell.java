package ss.spellid;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.Aspects;
import ss.spellid.block.ModBlocks;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.dream.DreamRealmLoader;
import ss.spellid.effect.ModEffects;
import ss.spellid.event.NightmareCompletionHandler;
import ss.spellid.event.SleepHandler;
import ss.spellid.event.EssenceRegenHandler;
import ss.spellid.event.WinterSolsticeHandler;
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
		ModBlocks.init();
		ModEffects.register();
		EssenceRegenHandler.register();

		// Copy Dream Realm files at server start
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			DreamRealmLoader.ensureDimensionFilesExist(server);
		});

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
		Aspects.init();
		WinterSolsticeHandler.register();

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

			// Nightmare exit command
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
						} else {
							player.displayClientMessage(Component.literal("§cYou are not in a nightmare!"), false);
						}
						return 1;
					}));

			// Spell seed commands – only PLAYER can receive seed
			dispatcher.register(Commands.literal("spell")
					.then(Commands.literal("seed")
							.then(Commands.literal("give")
									.executes(context -> {
										Player player = context.getSource().getPlayerOrException();
										var rankComp = RANK_KEY.get(player);
										if (rankComp.getRank() != Ranks.PLAYER) {
											player.displayClientMessage(Component.literal("§cOnly the Unawakened can receive the seed."), false);
											return 0;
										}
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

			// Aspect commands (optional – can be kept for testing)
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

			// Debug command for detailed stats
			dispatcher.register(Commands.literal("spell")
					.then(Commands.literal("debug")
							.then(Commands.literal("stats")
									.executes(context -> {
										ServerPlayer player = context.getSource().getPlayerOrException();
										var rankComp = RANK_KEY.get(player);
										var essenceComp = ESSENCE.get(player);
										var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
										var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
										var attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

										player.displayClientMessage(Component.literal("§6=== Player Stats ==="), false);
										player.displayClientMessage(Component.literal("§6Rank: §f" + rankComp.getRank().getDisplayName()), false);
										player.displayClientMessage(Component.literal("§6Health: §f" + player.getHealth() + " / " + player.getMaxHealth()), false);
										player.displayClientMessage(Component.literal("§6Speed: §f" + (speedAttr != null ? speedAttr.getValue() : 0)), false);
										player.displayClientMessage(Component.literal("§6Attack: §f" + (attackAttr != null ? attackAttr.getValue() : 0)), false);
										player.displayClientMessage(Component.literal("§6Essence: §f" + essenceComp.getCurrentEssence() + " / " + essenceComp.getMaxEssence()), false);
										player.displayClientMessage(Component.literal("§6Saturation: §f" + essenceComp.getSaturationProgress() + "/" + essenceComp.getSaturationMax()), false);
										return 1;
									}))
							.then(Commands.literal("regen")
									.executes(context -> {
										ServerPlayer player = context.getSource().getPlayerOrException();
										var essence = ESSENCE.get(player);
										int food = player.getFoodData().getFoodLevel();
										player.displayClientMessage(Component.literal("§6Food: §f" + food + " / 20"), false);
										player.displayClientMessage(Component.literal("§6Essence: §f" + essence.getCurrentEssence() + " / " + essence.getMaxEssence()), false);
										essence.tickRegen();
										player.displayClientMessage(Component.literal("§aAfter regen tick: " + essence.getCurrentEssence() + " / " + essence.getMaxEssence()), false);
										return 1;
									}))));

			// Test solstice command (already exists, but included for completeness)
			dispatcher.register(Commands.literal("spell")
					.then(Commands.literal("test")
							.then(Commands.literal("solstice")
									.executes(context -> {
										ServerPlayer player = context.getSource().getPlayerOrException();
										var essence = ESSENCE.get(player);
										long currentTime = player.level().getServer().overworld().getGameTime();
										essence.setSleeperStartTime(currentTime - (3 * 24000));
										WinterSolsticeHandler.forceTeleport(player);
										return 1;
									}))));
		});
	}
}