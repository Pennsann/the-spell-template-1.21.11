package ss.spellid.party;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyManager {
    private static final Map<UUID, Party> playerPartyMap = new HashMap<>();
    private static final Map<UUID, Party> partyIdMap = new HashMap<>();

    public static Party createParty(ServerPlayer leader, int maxSize) {
        if (playerPartyMap.containsKey(leader.getUUID())) return null;
        Party party = new Party(leader.getUUID(), maxSize);
        playerPartyMap.put(leader.getUUID(), party);
        partyIdMap.put(party.getPartyId(), party);
        return party;
    }

    public static Party getParty(ServerPlayer player) {
        return playerPartyMap.get(player.getUUID());
    }

    public static Party getPartyById(UUID partyId) {
        return partyIdMap.get(partyId);
    }

    public static boolean joinParty(ServerPlayer player, Party party) {
        UUID uuid = player.getUUID();
        if (playerPartyMap.containsKey(uuid)) return false;
        if (!party.hasInvite(uuid)) return false;
        if (party.isFull()) return false;
        party.addMember(uuid);
        playerPartyMap.put(uuid, party);
        party.removeInvite(uuid);
        return true;
    }

    public static boolean invitePlayer(ServerPlayer leader, ServerPlayer target) {
        Party party = getParty(leader);
        if (party == null || !party.isLeader(leader.getUUID())) return false;
        if (party.contains(target.getUUID())) return false;
        if (party.isFull()) return false;
        party.addInvite(target.getUUID());
        return true;
    }

    public static boolean leaveParty(ServerPlayer player) {
        Party party = getParty(player);
        if (party == null) return false;

        UUID uuid = player.getUUID();
        MinecraftServer server = player.level().getServer();

        party.removeMember(uuid);
        playerPartyMap.remove(uuid);

        // Notify remaining members
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.displayClientMessage(
                        Component.literal("§e" + player.getName().getString() + " left the party."), false);
            }
        }

        if (party.isLeader(uuid)) {
            if (party.size() > 0) {
                // Transfer leadership to next available member
                UUID newLeader = party.getMembers().iterator().next();
                party.setLeader(newLeader);
                ServerPlayer newLeaderPlayer = server.getPlayerList().getPlayer(newLeader);
                if (newLeaderPlayer != null) {
                    newLeaderPlayer.displayClientMessage(
                            Component.literal("§6You are now the party leader."), false);
                }
            } else {
                disbandParty(party, server);
            }
        } else {
            if (party.size() == 0) disbandParty(party, server);
        }

        return true;
    }

    public static void disbandParty(Party party, MinecraftServer server) {
        for (UUID member : party.getMembers()) {
            ServerPlayer memberPlayer = server.getPlayerList().getPlayer(member);
            if (memberPlayer != null) {
                memberPlayer.displayClientMessage(
                        Component.literal("§cYour party has been disbanded."), false);
            }
            playerPartyMap.remove(member);
        }
        partyIdMap.remove(party.getPartyId());
    }

    public static boolean kickPlayer(ServerPlayer leader, ServerPlayer target) {
        Party party = getParty(leader);
        if (party == null || !party.isLeader(leader.getUUID())) return false;
        if (!party.contains(target.getUUID())) return false;

        UUID targetUuid = target.getUUID();
        party.removeMember(targetUuid);
        playerPartyMap.remove(targetUuid);

        // Notify remaining members
        MinecraftServer server = leader.level().getServer();
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.displayClientMessage(
                        Component.literal("§e" + target.getName().getString() + " was kicked from the party."), false);
            }
        }

        if (party.size() == 0) disbandParty(party, server);
        return true;
    }

    public static boolean areInSameParty(ServerPlayer player1, ServerPlayer player2) {
        Party party = getParty(player1);
        return party != null && party.contains(player2.getUUID());
    }

    public static boolean isPartyMember(ServerPlayer player, LivingEntity target) {
        if (target instanceof ServerPlayer targetPlayer) {
            return areInSameParty(player, targetPlayer);
        }
        return false;
    }
}