package org.lazberry.xmaslegacy.role.passive.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;

import java.util.function.Consumer;

public abstract class PassiveListeners {
    private final Role role;
    private final UserManager um;

    protected UserManager userManager() {
        return um;
    }

    protected PassiveListeners(Role role, UserManager um) {
        this.role = role;
        this.um = um;
    }

    protected void canUsePassive(Player p, Consumer<User> success) {
        User user = um.getUser(p.getUniqueId());

        OptionalUtils.ifNotNullOrElse(user, u -> {
            if (!role.equals(u.getRole())) return;

            success.accept(u);
        }, () -> {
            InfoUtils.error(p, "유저 정보가 로드되지 않아 패시브가 작동하지 않습니다.");
            UserHandler.loadUser(p, true);
        });
    }

    protected void sendExpAlert(Player p, int amount) {
        p.sendActionBar(ColorUtils.chat("직업 경험치 &6+" + amount));
        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }
}
