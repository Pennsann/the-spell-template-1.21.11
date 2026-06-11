package ss.spellid.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.Aspects;
import ss.spellid.aspect.ability.ChanneledAbility;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.RankComponentInitializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChanneledAbilityHandler {
    private static final Map<UUID, ActiveChannel> activeChannels = new HashMap<>();

    private static class ActiveChannel {
        final ChanneledAbility ability;
        int tickCounter;
        final int interval;
        final int costPerTick;

        ActiveChannel(ChanneledAbility ability) {
            this.ability = ability;
            this.interval = ability.getTickInterval();
            this.costPerTick = ability.getEssenceCostPerTick();
            this.tickCounter = 0;
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                ActiveChannel channel = activeChannels.get(uuid);
                if (channel == null) continue;

                EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
                // Check cooldown (cooldown is enforced at start, not during channel)
                // Essence drain per tick
                if (essence.getCurrentEssence() < channel.costPerTick) {
                    stopChannel(player);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNot enough essence!"), true);
                    continue;
                }
                essence.addCurrentEssence(-channel.costPerTick);

                if (++channel.tickCounter >= channel.interval) {
                    channel.tickCounter = 0;
                    channel.ability.onTick(player);
                }
            }
        });
    }

    public static boolean startChannel(ServerPlayer player, ChanneledAbility ability) {
        UUID uuid = player.getUUID();
        if (activeChannels.containsKey(uuid)) return false;

        EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
        long now = player.level().getGameTime();
        // Check cooldown (store cooldown end in essence component with ability ID)
        String cooldownKey = "cooldown_" + ability.getId().toString();
        long cooldownEnd = essence.getCustomLong(cooldownKey, 0L);
        if (now < cooldownEnd) return false;

        // Check initial essence (at least one tick)
        if (essence.getCurrentEssence() < ability.getEssenceCostPerTick()) return false;

        ability.onStart(player);
        activeChannels.put(uuid, new ActiveChannel(ability));
        // Set cooldown for when the ability ends
        essence.setCustomLong(cooldownKey, now + ability.getCooldownTicks());
        return true;
    }

    public static void stopChannel(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ActiveChannel channel = activeChannels.remove(uuid);
        if (channel != null) {
            channel.ability.onStop(player);
        }
    }
}