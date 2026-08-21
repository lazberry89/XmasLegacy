package org.lazberry.xmaslegacy.role.passive;

import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;

import java.util.function.Consumer;

public abstract class PassiveListeners {
    private final UserManager um;

    protected PassiveListeners(UserManager um) {
        this.um = um;
    }

    protected void canUsePassive(Player p, Consumer<User> success) {
        User user = um.getUser(p.getUniqueId());
        OptionalUtils.ifNotNullOrElse(user, success, () -> {
            InfoUtils.error(p, "유저 정보가 로드되지 않아 패시브가 작동하지 않습니다.");
            UserHandler.loadUser(p, true);
        });
    }
}
