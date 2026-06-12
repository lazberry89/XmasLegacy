package org.lazberry.xmaslegacy.Party;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.*;

public enum PartyManager {
	INSTANCE;

    private final @NotNull UserManager um;
	private final @NotNull Map<User, Party> partyMap = new HashMap<>();

    PartyManager() {
        this.um = UserManager.INSTANCE;
    }

	public boolean createParty(@NotNull User leader) {
		if (partyMap.containsKey(leader)) return false;
		Party newParty = new Party(leader);

		partyMap.put(leader, newParty);
		return true;
	}

	public boolean joinParty(User leader, User join) {
		if (partyMap.containsKey(leader)) {
			Party party = partyMap.get(leader);
			if (party.joinParty(join)) {
				partyMap.put(join, party);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@SuppressWarnings("UnusedReturnValue")
    public boolean leaveParty(@NotNull User user) {
		if (partyMap.containsKey(user)) {
			Party party = partyMap.get(user);
			if (party != null) {
				partyMap.remove(user);
				party.leaveParty(user);

				if (party.getMembers().isEmpty()) party.clearMembers();
				return true;
			}
		}
		return false;
	}

	public boolean removeParty(@NotNull UUID uuid) {
		User user = um.getUser(uuid);
		if (user == null) return false;
		Party currentParty = partyMap.get(user);
		if (currentParty == null) return false;

		User leader = currentParty.getLeader();
		if (!leader.equals(user)) return false;

		currentParty.getMembers().forEach(partyMap::remove);

		currentParty.clearMembers();
		return true;
	}

    public Party getParty(UUID p) {
		User u = um.getUser(p);
        return partyMap.get(u);
    }

    public boolean isInParty(UUID p) {
		User u = um.getUser(p);
        return partyMap.containsKey(u);
    }

    public boolean isLeader(UUID p) {
        Party party = getParty(p);
	    if (party == null) return false;
        return party.getLeader().getUUID().equals(p);
    }

    public boolean isParty(UUID p1, UUID p2) {
        Party party = getParty(p1);
        if (party == null) return false;

        return party.getMembers().stream()
                .anyMatch(user -> user.getUUID().equals(p2));
    }
}