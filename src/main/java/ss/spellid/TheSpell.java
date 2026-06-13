package ss.spellid;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.arguments.EntityArgument;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.Aspects;
import ss.spellid.aspect.ability.AspectAbility;
import ss.spellid.aspect.ability.ChanneledAbility;
import ss.spellid.block.ModBlockEntities;
import ss.spellid.block.ModBlocks;
import ss.spellid.components.RankComponentInitializer;
import ss.spellid.dream.DreamRealmLoader;
import ss.spellid.effect.ModEffects;
import ss.spellid.event.*;
import ss.spellid.item.ModItems;
import ss.spellid.network.ChannelStartPayload;
import ss.spellid.network.ChannelStopPayload;
import ss.spellid.nightmare.Nightmare;
import ss.spellid.nightmare.NightmareManager;
import ss.spellid.party.Party;
import ss.spellid.party.PartyManager;
import ss.spellid.ranks.Ranks;

import java.util.Set;
import java.util.UUID;

import static ss.spellid.components.RankComponentInitializer.RANK_KEY;
import static ss.spellid.components.RankComponentInitializer.ESSENCE;

public class TheSpell implements ModInitializer {
	public static final String MOD_ID = "the-spell";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String AUTHOR_NAME = "Fexolion";

	@Override
	public void onInitialize() {
		LOGGER.info(MOD_ID + " initialized");

		ModItems.init();
		ModBlocks.init();
		ModBlockEntities.register();
		ModEffects.register();
		Aspects.init();

		EssenceRegenHandler.register();
		AttackHandler.register();
		SleepHandler.register();
		NightmareCompletionHandler.register();
		WinterSolsticeHandler.register();
		AuraHandler.register();
		ChanneledAbilityHandler.register();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			DreamRealmLoader.ensureDimensionFilesExist(server);
		});

		// Join event: display rank and grant exclusive aspect to author
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			try {
				Player player = handler.player;
				var rankComp = RANK_KEY.get(player);
				player.displayClientMessage(Component.literal("§e[Spell] Your current rank: " + rankComp.getRank().getDisplayName()), false);
				LOGGER.info("Player {} joined with rank {}", player.getName().getString(), rankComp.getRank());

				if (player.getName().getString().equals(AUTHOR_NAME)) {
					var essence = ESSENCE.get(player);
					if (!Aspects.FROST_OF_THE_LONELY_PEAK.getId().toString().equals(essence.getAspectId())) {
						essence.setAspectId(Aspects.FROST_OF_THE_LONELY_PEAK.getId().toString());
						player.displayClientMessage(Component.literal("§bYou feel the cold embrace of the Lonely Peak... Your unique aspect awakens."), false);
						LOGGER.info("Granted exclusive aspect to author {}", player.getName().getString());
					}
					if (rankComp.getRank().ordinal() < Ranks.SLEEPER.ordinal()) {
						rankComp.setRank(Ranks.SLEEPER);
						player.displayClientMessage(Component.literal("§7The Spell recognizes you. You are now a Sleeper."), false);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error in player join event", e);
			}
		});

		registerNetworkReceivers();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private void registerNetworkReceivers() {
		PayloadTypeRegistry.playC2S().register(ChannelStartPayload.TYPE, ChannelStartPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ChannelStopPayload.TYPE, ChannelStopPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ChannelStartPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				int slot = payload.slot();
				var rankComp = RANK_KEY.get(player);
				var essence = ESSENCE.get(player);

				if (slot < 0 || slot > 2) {
					player.displayClientMessage(Component.literal("§cInvalid ability slot!"), true);
					return;
				}

				String aspectId = essence.getAspectId();
				if (aspectId == null) {
					player.displayClientMessage(Component.literal("§cYou have no aspect!"), true);
					return;
				}
				Aspect aspect = Aspects.get(Identifier.parse(aspectId));
				if (aspect == null) return;
				AspectAbility ability = aspect.getAbilityForSlot(slot);
				if (ability == null) {
					player.displayClientMessage(Component.literal("§cYour aspect has no ability for this slot!"), true);
					return;
				}

				if (rankComp.getRank().ordinal() < ability.getRequiredRank().ordinal()) {
					player.displayClientMessage(Component.literal("§cYou need to be " + ability.getRequiredRank().getDisplayName() + " to use this ability!"), true);
					return;
				}

				if (ability instanceof ChanneledAbility channeled) {
					if (!ChanneledAbilityHandler.startChannel(player, channeled)) {
						player.displayClientMessage(Component.literal("§cCannot start channeled ability (cooldown or already active)!"), true);
					}
				} else {
					long currentTime = player.level().getGameTime();
					String cooldownKey = "cooldown_" + ability.getId().toString();
					long cooldownEnd = essence.getCustomLong(cooldownKey, 0L);
					if (currentTime < cooldownEnd) {
						long ticksLeft = cooldownEnd - currentTime;
						player.displayClientMessage(Component.literal("§cAbility on cooldown! (" + (ticksLeft / 20) + "s)"), true);
						return;
					}
					int cost = ability.getEssenceCost();
					if (essence.getCurrentEssence() < cost) {
						player.displayClientMessage(Component.literal("§cNot enough essence!"), true);
						return;
					}
					if (ability.canUse(player)) {
						ability.use(player);
						essence.addCurrentEssence(-cost);
						essence.setCustomLong(cooldownKey, currentTime + ability.getCooldownTicks());
						player.displayClientMessage(Component.literal("§aUsed " + ability.getId().getPath()), true);
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ChannelStopPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				ChanneledAbilityHandler.stopChannel(player);
			});
		});
	}

	private void registerCommands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
		// Soul debug command
		dispatcher.register(Commands.literal("soul_debug")
				.executes(context -> {
					Player player = context.getSource().getPlayerOrException();
					var rankComp = RANK_KEY.get(player);
					var essenceComp = ESSENCE.get(player);
					String aspectDisplay = essenceComp.getAspectId() != null ?
							Aspects.get(Identifier.parse(essenceComp.getAspectId())).getDisplayName().getString() : "None";
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
					Identifier nightmareId = Identifier.fromNamespaceAndPath(MOD_ID, "first_nightmare");
					ResourceKey<Level> nightmareKey = ResourceKey.create(Registries.DIMENSION, nightmareId);
					if (player.level().dimension().equals(nightmareKey)) {
						ServerPlayer serverPlayer = (ServerPlayer) player;
						ServerLevel overworld = serverPlayer.level().getServer().overworld();
						double x = overworld.getRespawnData().pos().getX();
						double y = overworld.getRespawnData().pos().getY();
						double z = overworld.getRespawnData().pos().getZ();
						serverPlayer.teleportTo(overworld, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
						player.displayClientMessage(Component.literal("§aYou escape the nightmare... for now."), false);
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
									var rankComp = RANK_KEY.get(player);
									if (rankComp.getRank() != Ranks.PLAYER) {
										player.displayClientMessage(Component.literal("§cOnly the Unawakened can receive the seed."), false);
										return 0;
									}
									var essence = ESSENCE.get(player);
									essence.setNightmareSeed(true);
									if (player instanceof ServerPlayer sp) {
										sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.NIGHTMARE_SEED, -1, 0, false, true, true));
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

		// Aspect get/set
		dispatcher.register(Commands.literal("spell")
				.then(Commands.literal("aspect")
						.then(Commands.literal("get")
								.executes(context -> {
									Player player = context.getSource().getPlayerOrException();
									var essence = ESSENCE.get(player);
									String id = essence.getAspectId();
									if (id == null) {
										player.displayClientMessage(Component.literal("§cYou have no aspect."), false);
									} else {
										Aspect aspect = Aspects.get(Identifier.parse(id));
										player.displayClientMessage(Component.literal("§6Your aspect: §f" + (aspect != null ? aspect.getDisplayName().getString() : "Unknown")), false);
									}
									return 1;
								}))
						.then(Commands.literal("set")
								.then(Commands.argument("id", StringArgumentType.string())
										.executes(context -> {
											String id = context.getArgument("id", String.class);
											Player player = context.getSource().getPlayerOrException();
											Identifier aspectId = id.contains(":") ? Identifier.parse(id) : Identifier.fromNamespaceAndPath(MOD_ID, id);
											Aspect aspect = Aspects.get(aspectId);
											if (aspect == null) {
												player.displayClientMessage(Component.literal("§cAspect not found: " + id), false);
												return 0;
											}
											var essence = ESSENCE.get(player);
											essence.setAspectId(aspectId.toString());
											player.displayClientMessage(Component.literal("§aAspect set to " + aspect.getDisplayName().getString()), false);
											return 1;
										})))));

		// Debug commands: stats, regen, setrank, fillessence, test solstice
		dispatcher.register(Commands.literal("spell")
				.then(Commands.literal("debug")
						.then(Commands.literal("stats")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									var rankComp = RANK_KEY.get(player);
									var essenceComp = ESSENCE.get(player);
									player.displayClientMessage(Component.literal("§6=== Player Stats ==="), false);
									player.displayClientMessage(Component.literal("§6Rank: §f" + rankComp.getRank().getDisplayName()), false);
									player.displayClientMessage(Component.literal("§6Health: §f" + player.getHealth() + " / " + player.getMaxHealth()), false);
									player.displayClientMessage(Component.literal("§6Speed: §f" + (player.getAttribute(Attributes.MOVEMENT_SPEED) != null ? player.getAttribute(Attributes.MOVEMENT_SPEED).getValue() : 0)), false);
									player.displayClientMessage(Component.literal("§6Attack: §f" + (player.getAttribute(Attributes.ATTACK_DAMAGE) != null ? player.getAttribute(Attributes.ATTACK_DAMAGE).getValue() : 0)), false);
									player.displayClientMessage(Component.literal("§6Essence: §f" + essenceComp.getCurrentEssence() + " / " + essenceComp.getMaxEssence()), false);
									player.displayClientMessage(Component.literal("§6Saturation: §f" + essenceComp.getSaturationProgress() + "/" + essenceComp.getSaturationMax()), false);
									return 1;
								}))
						.then(Commands.literal("regen")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									var essence = ESSENCE.get(player);
									essence.tickRegen();
									player.displayClientMessage(Component.literal("§aRegen ticked, essence now " + essence.getCurrentEssence()), false);
									return 1;
								}))
						.then(Commands.literal("setrank")
								.then(Commands.argument("rank", StringArgumentType.string())
										.executes(context -> {
											String rankName = context.getArgument("rank", String.class);
											ServerPlayer player = context.getSource().getPlayerOrException();
											try {
												Ranks newRank = Ranks.valueOf(rankName.toUpperCase());
												var rankComp = RANK_KEY.get(player);
												rankComp.setRank(newRank);
												player.displayClientMessage(Component.literal("§aRank set to " + newRank.getDisplayName()), false);
											} catch (IllegalArgumentException e) {
												player.displayClientMessage(Component.literal("§cInvalid rank. Use: PLAYER, SLEEPER, AWAKENED, ASCENDED, TRANSCENDENT, SUPREME, SACRED, DIVINE"), false);
											}
											return 1;
										})))
						.then(Commands.literal("fillessence")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									var essence = ESSENCE.get(player);
									int max = essence.getMaxEssence();
									essence.setCurrentEssence(max);
									player.displayClientMessage(Component.literal("§aEssence filled to " + max + " / " + max), false);
									return 1;
								}))
				));

		// Test solstice command
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

		// ---------- PARTY COMMANDS ----------
		dispatcher.register(Commands.literal("party")
				.then(Commands.literal("create")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							Party party = PartyManager.createParty(player, 5);
							if (party == null) {
								player.displayClientMessage(Component.literal("§cYou are already in a party or could not create one."), false);
							} else {
								player.displayClientMessage(Component.literal("§aYou created a new party."), false);
							}
							return 1;
						}))
				.then(Commands.literal("invite")
						.then(Commands.argument("target", StringArgumentType.word())
								.suggests((context, builder) -> {
									for (ServerPlayer p : context.getSource().getServer().getPlayerList().getPlayers()) {
										builder.suggest(p.getName().getString());
									}
									return builder.buildFuture();
								})
								.executes(context -> {
									ServerPlayer sender = context.getSource().getPlayerOrException();
									String targetName = context.getArgument("target", String.class);
									ServerPlayer target = sender.level().getServer().getPlayerList().getPlayerByName(targetName);
									if (target == null) {
										sender.displayClientMessage(Component.literal("§cPlayer not found."), false);
										return 0;
									}
									if (PartyManager.invitePlayer(sender, target)) {
										sender.displayClientMessage(Component.literal("§aInvited " + target.getName().getString() + " to your party."), false);
										target.displayClientMessage(Component.literal("§6You have been invited to " + sender.getName().getString() + "'s party. Use §e/party join " + sender.getName().getString() + " §6to accept."), false);
									} else {
										sender.displayClientMessage(Component.literal("§cCould not invite player (maybe not leader, party full, or already in party)."), false);
									}
									return 1;
								})))
				.then(Commands.literal("join")
						.then(Commands.argument("leader", StringArgumentType.word())
								.suggests((context, builder) -> {
									for (ServerPlayer p : context.getSource().getServer().getPlayerList().getPlayers()) {
										builder.suggest(p.getName().getString());
									}
									return builder.buildFuture();
								})
								.executes(context -> {
									ServerPlayer joiner = context.getSource().getPlayerOrException();
									String leaderName = context.getArgument("leader", String.class);
									ServerPlayer leader = joiner.level().getServer().getPlayerList().getPlayerByName(leaderName);
									if (leader == null) {
										joiner.displayClientMessage(Component.literal("§cLeader not found."), false);
										return 0;
									}
									Party party = PartyManager.getParty(leader);
									if (party == null) {
										joiner.displayClientMessage(Component.literal("§cThat player is not in a party."), false);
										return 0;
									}
									if (!party.hasInvite(joiner.getUUID())) {
										joiner.displayClientMessage(Component.literal("§cYou have not been invited to that party."), false);
										return 0;
									}
									if (PartyManager.joinParty(joiner, party)) {
										joiner.displayClientMessage(Component.literal("§aYou joined the party."), false);
										for (UUID memberId : party.getMembers()) {
											if (!memberId.equals(joiner.getUUID())) {
												ServerPlayer member = joiner.level().getServer().getPlayerList().getPlayer(memberId);
												if (member != null) {
													member.displayClientMessage(Component.literal("§e" + joiner.getName().getString() + " joined the party."), false);
												}
											}
										}
									} else {
										joiner.displayClientMessage(Component.literal("§cCould not join party (maybe full or already in a party)."), false);
									}
									return 1;
								})))
				.then(Commands.literal("leave")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							if (PartyManager.leaveParty(player)) {
								player.displayClientMessage(Component.literal("§aYou left your party."), false);
							} else {
								player.displayClientMessage(Component.literal("§cYou are not in a party."), false);
							}
							return 1;
						}))
				.then(Commands.literal("kick")
						.then(Commands.argument("target", StringArgumentType.word())
								.suggests((context, builder) -> {
									Party party = PartyManager.getParty(context.getSource().getPlayerOrException());
									if (party != null) {
										for (UUID memberId : party.getMembers()) {
											ServerPlayer member = context.getSource().getServer().getPlayerList().getPlayer(memberId);
											if (member != null) {
												builder.suggest(member.getName().getString());
											}
										}
									}
									return builder.buildFuture();
								})
								.executes(context -> {
									ServerPlayer kicker = context.getSource().getPlayerOrException();
									String targetName = context.getArgument("target", String.class);
									ServerPlayer target = kicker.level().getServer().getPlayerList().getPlayerByName(targetName);
									if (target == null) {
										kicker.displayClientMessage(Component.literal("§cPlayer not found."), false);
										return 0;
									}
									if (PartyManager.kickPlayer(kicker, target)) {
										kicker.displayClientMessage(Component.literal("§aKicked " + targetName + " from the party."), false);
										target.displayClientMessage(Component.literal("§cYou were kicked from your party."), false);
									} else {
										kicker.displayClientMessage(Component.literal("§cCould not kick player (maybe not leader or not in party)."), false);
									}
									return 1;
								})))
				.then(Commands.literal("disband")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							Party party = PartyManager.getParty(player);
							if (party != null && party.isLeader(player.getUUID())) {
								PartyManager.disbandParty(party, player.level().getServer());
								player.displayClientMessage(Component.literal("§aParty disbanded."), false);
							} else {
								player.displayClientMessage(Component.literal("§cYou are not the party leader."), false);
							}
							return 1;
						}))
				.then(Commands.literal("list")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							Party party = PartyManager.getParty(player);
							if (party == null) {
								player.displayClientMessage(Component.literal("§cYou are not in a party."), false);
								return 0;
							}
							StringBuilder sb = new StringBuilder("§6Party Members: ");
							for (UUID memberId : party.getMembers()) {
								ServerPlayer member = player.level().getServer().getPlayerList().getPlayer(memberId);
								if (member != null) {
									if (member.getUUID().equals(party.getLeader())) {
										sb.append("§e* ").append(member.getName().getString()).append(" §f(Leader)§r, ");
									} else {
										sb.append("§f- ").append(member.getName().getString()).append("§r, ");
									}
								}
							}
							String msg = sb.toString();
							if (msg.endsWith(", ")) msg = msg.substring(0, msg.length() - 2);
							player.displayClientMessage(Component.literal(msg), false);
							return 1;
						}))
		);

		// Nightmare seed command (for testing)
		dispatcher.register(Commands.literal("nightmare_seed")
				.then(Commands.argument("nightmare", StringArgumentType.greedyString())
						.executes(context -> {
							String idStr = context.getArgument("nightmare", String.class);
							Identifier nightmareId;
							try {
								nightmareId = Identifier.parse(idStr);
							} catch (Exception e) {
								context.getSource().sendFailure(Component.literal("Invalid nightmare ID format! Use namespace:path"));
								return 0;
							}
							Nightmare nightmare = NightmareManager.get(nightmareId);
							if (nightmare == null) {
								context.getSource().sendFailure(Component.literal("Nightmare not found!"));
								return 0;
							}
							ServerPlayer player = context.getSource().getPlayerOrException();
							ItemStack seedItem = new ItemStack(ModBlocks.NIGHTMARE_SEED.asItem());
							CompoundTag tag = new CompoundTag();
							tag.putString("nightmare_id", nightmareId.toString());
							seedItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
							player.getInventory().add(seedItem);
							player.displayClientMessage(Component.literal("§aYou received a Nightmare Seed item for " + nightmare.displayName()), false);
							return 1;
						})));
	}
}