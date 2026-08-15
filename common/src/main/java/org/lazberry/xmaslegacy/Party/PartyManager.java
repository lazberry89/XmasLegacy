package org.lazberry.xmaslegacy.Party;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Exclude(type = ServerType.LOBBY)
public class PartyManager implements Initiator {
	private final @NotNull UserManager um;
	private final @NotNull Map<User, Party> partyMap = new ConcurrentHashMap<>();

	@Inject
	public PartyManager(@NotNull UserManager um) {
		this.um = um;
	}

	@Override
	public void init() {}

	public boolean createParty(@NotNull User leader) {
		if (partyMap.containsKey(leader)) return false;

		Party newParty = new Party(leader);
		partyMap.put(leader, newParty);
		return true;
	}

	public boolean joinParty(@NotNull User leader, @NotNull User join) {
		if (partyMap.containsKey(join)) return false;

		Party party = partyMap.get(leader);
		if (party == null) return false;

		if (!party.getLeader().equals(leader)) return false;

		if (party.joinParty(join)) {
			partyMap.put(join, party);
			return true;
		}
		return false;
	}

	public boolean leaveParty(@NotNull User user) {
		Party party = partyMap.get(user);
		if (party == null) return false;

		partyMap.remove(user);

		boolean isStillAlive = party.leaveParty(user);

		if (!isStillAlive) {
			party.clearMembers();
		} else {
			User newLeader = party.getLeader();
			partyMap.put(newLeader, party);
		}
		return true;
	}

	public boolean removeParty(@NotNull UUID uuid) {
		User user = um.getUser(uuid);
		if (user == null) return false;

		Party currentParty = partyMap.get(user);
		if (currentParty == null) return false;

		if (!currentParty.getLeader().equals(user)) return false;

		List<User> targets = new ArrayList<>(currentParty.getMembers());
		for (User target : targets) {
			partyMap.remove(target);
		}

		currentParty.clearMembers();
		return true;
	}

	public boolean removeParty(@NotNull Party party) {
		if (party.getMembers().isEmpty()) return false;
		List<User> targets = new ArrayList<>(party.getMembers());

		for (User target : targets) partyMap.remove(target);

		party.clearMembers();
		return true;
	}
	public @Nullable Party getParty(@NotNull UUID uuid) {
		User u = um.getUser(uuid);
		if (u == null) return null;
		return partyMap.get(u);
	}

	public boolean isInParty(@NotNull UUID uuid) {
		User u = um.getUser(uuid);
		if (u == null) return false;
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

	public boolean handOverLeadership(@NotNull UUID currentLeaderUuid, @NotNull UUID newLeaderUuid) {
		Party party = getParty(currentLeaderUuid);
		if (party == null) return false;

		if (!party.getLeader().getUniqueId().equals(currentLeaderUuid)) return false;

		User newLeader = um.getUser(newLeaderUuid);
		if (newLeader == null) return false;

		return party.changeLeader(newLeader);
	}
}