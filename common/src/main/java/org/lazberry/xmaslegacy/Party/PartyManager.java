package org.lazberry.xmaslegacy.Party;

import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.*;

public class PartyManager {
    private final UserManager UM;
	private final Map<User, Party> partyMap = new HashMap<>();

    public PartyManager(UserManager UM) {
        this.UM = UM;
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

	public boolean leaveParty(User user) {
		if (partyMap.containsKey(user)) {
			Party party = partyMap.get(user);
			if (party != null) {
				party.leaveParty(user);
				partyMap.remove(user);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

    public boolean removeParty(UUID uuid) {
		User user = UM.getUser(uuid);
        if (user == null) return false;
        Party currentParty = partyMap.get(user);
        if (currentParty == null) return false;

        User leader = currentParty.getLeader();
        if (!leader.equals(user)) return false;

        for (User member : currentParty.getMembers()) {
            partyMap.remove(member);
        }
		return true;
    }

    public Party getParty(UUID p) {
		User u = UM.getUser(p);
        return partyMap.get(u);
    }

    public boolean isInParty(UUID p) {
		User u = UM.getUser(p);
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