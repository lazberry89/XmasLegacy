package org.lazberry.xmaslegacy.Party;

import org.lazberry.xmaslegacy.User.User;

import java.util.ArrayList;
import java.util.List;

public class Party {
    private final User leader;
    private final List<User> members = new ArrayList<>();

    public Party(User leader) {
        this.leader = leader;
        this.members.add(leader);
    }

    public boolean joinParty(User join) {
        if (members.contains(join) || members.size() >= 4) {
            return false;
        }
        members.add(join);
        return true;
    }

    public boolean leaveParty(User leave) {
        if (members.contains(leave)) {
            if (leave.equals(leader)) {
                members.remove(leader);
                if (!members.isEmpty()) {

                }
            } else {
                members.remove(leave);
                return true;
            }
        }
        return false;
    }


}
