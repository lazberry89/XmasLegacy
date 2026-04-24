package org.lazberry.xmaslegacy;

import org.lazberry.xmaslegacy.User.User;

import java.util.List;
import java.util.UUID;

public class PartyQuitResult {

	private final UUID leaverUuid;
	private final List<User> remainingMembers;
	private final User newLeader;

	public PartyQuitResult(UUID leaverUuid, List<User> remainingMembers, User newLeader) {
		this.leaverUuid = leaverUuid;
		this.remainingMembers = remainingMembers;
		this.newLeader = newLeader;
	}

	public List<User> getRemainingMembers() { return remainingMembers; }
	public User getNewLeader() { return newLeader; }

}
