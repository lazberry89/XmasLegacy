package org.lazberry.xmaslegacy.Party;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
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
	private @Getter User leader;
	private final @NotNull List<User> members = new ArrayList<>();

	public Party(@NotNull User leader) {
		this.partyID = UUID.randomUUID();
		this.leader = leader;
		this.members.add(leader);
	}

	public boolean changeLeader(@NotNull User newLeader) {
		if (!members.contains(newLeader)) return false;

		this.leader = newLeader;
		return true;
	}

	public boolean joinParty(@NotNull User join) {
		if (members.contains(join) || isFull()) {
			return false;
		}
		return members.add(join);
	}

	public boolean leaveParty(@NotNull User leave) {
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

	public @NotNull List<User> getMembers() {
		return Collections.unmodifiableList(members);
	}

	public boolean isFull() {
		return members.size() >= 4;
	}

	public boolean isInParty(@NotNull User user) {
		return members.contains(user);
	}

	public void clearMembers() {
		members.clear();
		leader = null;
	}
}