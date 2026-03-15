package ss.spellid.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import ss.spellid.TheSpell;

import java.util.Set;

public class NightmareExitCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nightmare_exit")
                .executes(context -> {
                    Player player = context.getSource().getPlayerOrException();

                    Identifier dimensionId = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "first_nightmare");
                    ResourceKey<Level> nightmareKey = ResourceKey.create(
                            Registries.DIMENSION,
                            dimensionId
                    );

                    if (player.level().dimension().equals(nightmareKey)) {
                        ServerPlayer serverPlayer = (ServerPlayer) player;

                        // ✅ Get overworld via level().getServer()
                        ServerLevel overworld = serverPlayer.level().getServer().overworld();

                        // ✅ Get spawn coordinates
                        double x = overworld.getRespawnData().pos().getX();
                        double y = overworld.getRespawnData().pos().getY();
                        double z = overworld.getRespawnData().pos().getZ();

                        // ✅ Teleport back using the correct method
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
                })
        );
    }
}