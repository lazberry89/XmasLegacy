package org.lazberry.xmaslegacy.party;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Party {
	@EqualsAndHashCode.Include
	private final @NotNull UUID partyID;
	private @Getter UUID leader;
	private final @NotNull List<UUID> members = new ArrayList<>();

	public Party(@NotNull UUID leader) {
		this.partyID = UUID.randomUUID();
		this.leader = leader;
		this.members.add(leader);
	}

	public Party(User user) {
		this(user.getUniqueId());
	}

	public boolean changeLeader(UUID newLeader) {
		if (!members.contains(newLeader)) return false;

		this.leader = newLeader;
		return true;
	}

	public boolean joinParty(UUID join) {
		if (members.contains(join) || isFull()) {
			return false;
		}
		return members.add(join);
	}

	public boolean leaveParty(UUID leave) {
		if (!members.contains(leave)) return false;

		if (leave.equals(leader)) {
			members.remove(leave);

			if (!members.isEmpty()) {
				changeLeader(members.getFirst());
				return true;
			} else {
				leader = null;
				return false;
			}
		}
		return members.remove(leave);
	}

	public @NotNull UUID getPartyID() {
		return partyID;
	}

	public @NotNull List<UUID> getMembers() {
		return Collections.unmodifiableList(members);
	}

	public boolean isFull() {
		return members.size() >= 4;
	}

	public boolean isInParty(UUID user) {
		return members.contains(user);
	}

	public void clearMembers() {
		members.clear();
		leader = null;
	}
}