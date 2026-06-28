package org.lazberry.xmaslegacy.Party;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public boolean leaveParty(@NotNull User user) {
		if (partyMap.containsKey(user)) {
			Party party = partyMap.get(user);
			if (party != null) {
				partyMap.remove(user);
				party.leaveParty(user);

				if (party.getMembers().isEmpty()) {
					party.clearMembers();
					party = null;
				}
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

    public @Nullable Party getParty(@NotNull UUID uuid) {
		User u = um.getUser(uuid);
        return partyMap.get(u);
    }

    public boolean isInParty(@NotNull UUID uuid) {
		User u = um.getUser(uuid);
        return partyMap.containsKey(u);
    }

    public boolean isLeader(@NotNull UUID uuid) {
        Party party = getParty(uuid);
	    if (party == null) return false;
        return party.getLeader().getUniqueId().equals(uuid);
    }

    public boolean isParty(@NotNull UUID uuid1, @NotNull UUID uuid2) {
        Party party = getParty(uuid1);
        if (party == null) return false;

        return party.getMembers().stream()
                .anyMatch(user -> user.getUniqueId().equals(uuid2));
    }
}