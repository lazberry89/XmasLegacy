package org.lazberry.xmaslegacy.Party;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Party {
    private @NotNull User leader;
	private final @NotNull UUID PartyID;
    private final @NotNull List<User> members = new ArrayList<>();

    public Party(@NotNull User leader) {
		this.PartyID = UUID.randomUUID();
        this.leader = leader;
        this.members.add(leader);
    }

    public boolean joinParty(@NotNull User join) {
        if (members.contains(join) || members.size() >= 4) {
            return false;
        }
        members.add(join);
        return true;
    }

	public void clearMembers() {
		this.members.clear();
	}

	@SuppressWarnings("UnusedReturnValue")
    public boolean leaveParty(@NotNull User leave) {
        if (members.contains(leave)) {
            if (leave.equals(leader)) {
                members.remove(leader);
                if (!members.isEmpty()) {
					leader = members.getFirst();
					return true;
                } else {
					return false;
                }
            } else {
                members.remove(leave);
                return true;
            }
        }
        return false;
    }

	public @NotNull User getLeader() {
		return leader;
	}
	public @NotNull List<User> getMembers() {
		return members;
	}
	public boolean isFull() {
		return members.size() >= 4;
	}
	public boolean isInParty(@NotNull User user) {
		return members.contains(user);
	}

	public boolean isInParty(@NotNull UUID uuid) {
		@NotNull var um = UserManager.getInstance();
		User u = um.getUser(uuid);
		if (u == null) return false;
		return isInParty(u);
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Party u)) return false;
		return PartyID.equals(u.PartyID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(PartyID);
	}

}
