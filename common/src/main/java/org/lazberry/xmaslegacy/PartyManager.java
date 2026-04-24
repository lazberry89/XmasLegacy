package org.lazberry.xmaslegacy;

import java.util.*;

public class PartyManager {
    private final UserManager UM;
    private final Map<UUID, List<User>> userPartyMap = new HashMap<>();

    public PartyManager(UserManager UM) {
        this.UM = UM;
    }

    public List<User> createParty(UUID uuid) {
        if (userPartyMap.containsKey(uuid)) return userPartyMap.get(uuid);

        List<User> newParty = new ArrayList<>();
        newParty.add(UM.getUser(uuid));
        userPartyMap.put(uuid, newParty);
        return newParty;
    }

    public boolean joinParty(UUID joiner, UUID target) {
        List<User> targetParty = userPartyMap.get(target);
        if (targetParty == null) return false;

        if (targetParty.size() >= 4) return false;
        if (userPartyMap.containsKey(joiner)) return false;

        User u = UM.getUser(joiner);
        targetParty.add(u);
        userPartyMap.put(joiner, targetParty);
        return true;
    }

	public PartyQuitResult quitParty(UUID uuid) {
		List<User> currentParty = userPartyMap.get(uuid);
		if (currentParty == null) return null;

		User u = UM.getUser(uuid);
		userPartyMap.remove(uuid);
		currentParty.remove(u);

		User newLeader = null;
		if (!currentParty.isEmpty()) {
			newLeader = currentParty.getFirst();
		}

		return new PartyQuitResult(uuid, new ArrayList<>(currentParty), newLeader);
	}

    public boolean removeParty(UUID uuid) {
        List<User> currentParty = userPartyMap.get(uuid);
        if (currentParty == null) return false;

        User leader = currentParty.getFirst();
        if (!leader.getUUID().equals(uuid)) return false;

        for (User member : currentParty) {
            userPartyMap.remove(member.getUUID());
        }

        currentParty.clear();
        return true;
    }

    public List<User> getParty(UUID p) {
        return userPartyMap.get(p);
    }

    public boolean isInParty(UUID p) {
        return userPartyMap.containsKey(p);
    }

    public boolean isLeader(UUID p) {
        List<User> party = getParty(p);
        if (party == null || party.isEmpty()) return false;

        return party.getFirst().getUUID().equals(p);
    }

    public boolean isParty(UUID p1, UUID p2) {
        List<User> party = getParty(p1);
        if (party == null) return false;

        return party.stream()
                .anyMatch(user -> user.getUUID().equals(p2));
    }
}