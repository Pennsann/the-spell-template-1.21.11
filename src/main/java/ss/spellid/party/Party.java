package ss.spellid.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final UUID partyId;
    private UUID leader;
    private final Set<UUID> members;
    private final Set<UUID> invites;
    private final int maxSize;

    public Party(UUID leader, int maxSize) {
        this.partyId = UUID.randomUUID();
        this.leader = leader;
        this.maxSize = maxSize;
        this.members = new HashSet<>();
        this.members.add(leader);
        this.invites = new HashSet<>();
    }

    public UUID getPartyId() { return partyId; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID newLeader) { this.leader = newLeader; }
    public Set<UUID> getMembers() { return Set.copyOf(members); }
    public Set<UUID> getInvites() { return Set.copyOf(invites); }
    public int getMaxSize() { return maxSize; }
    public boolean isFull() { return members.size() >= maxSize; }

    public boolean addMember(UUID player) {
        if (isFull()) return false;
        return members.add(player);
    }

    public boolean removeMember(UUID player) {
        return members.remove(player);
    }

    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }

    public boolean contains(UUID player) {
        return members.contains(player);
    }

    public void addInvite(UUID invited) {
        invites.add(invited);
    }

    public void removeInvite(UUID invited) {
        invites.remove(invited);
    }

    public boolean hasInvite(UUID invited) {
        return invites.contains(invited);
    }

    public int size() { return members.size(); }
}