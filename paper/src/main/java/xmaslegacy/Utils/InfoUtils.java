package xmaslegacy.Utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;

public final class InfoUtils {

    @ApiStatus.Internal
    private InfoUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void infoMsg(@NotNull InfoLevel level, @NotNull Player p, @NotNull String msg) {
        Component txt = ColorUtils.chat(level.Prefix() + " " + msg);
        p.sendMessage(txt);
        p.playSound(p, level.Sound(), 1.0f, 1.0f);
        var user = UserManager.INSTANCE.getUser(p.getUniqueId());
        if (user == null) {
            ServerTransfer.sendReloadNotice(p);
            return;
        }
        if (user.isMobile()) p.sendActionBar(txt);
    }
}
