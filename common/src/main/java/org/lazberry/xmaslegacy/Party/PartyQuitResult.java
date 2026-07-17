package org.lazberry.xmaslegacy.Party;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.List;
import java.util.UUID;

@ConsumableClass
public class PartyQuitResult {

	private final @NotNull @Getter UUID leaverUuid;
	private final @NotNull @Getter List<User> remainingMembers;
	private final @NotNull @Getter User newLeader;

	public PartyQuitResult(@NotNull UUID leaverUuid, @NotNull List<User> remainingMembers, @NotNull User newLeader) {
		this.leaverUuid = leaverUuid;
		this.remainingMembers = remainingMembers;
		this.newLeader = newLeader;
	}
}
