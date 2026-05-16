package org.lazberry.xmaslegacy.settings;

import org.lazberry.xmaslegacy.User.User;

public interface Unreplicable {
    OwnerState use(User user);
    boolean isUsed();
    interface OwnerState {
        User getUser();
    }
}
