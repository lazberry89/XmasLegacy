package xmaslegacy.Utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Utility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;

@UtilityClass
public final class InfoUtils {

    public void error(@NotNull Player p, @NotNull String msg) {
        sendMessage(InfoLevel.ERROR, p, msg);
    }
    public void error(@NotNull Player p, @NotNull Component msg) {
        sendMessage(InfoLevel.ERROR, p, msg);
    }
    public void warn(@NotNull Player p, @NotNull String msg) {
        sendMessage(InfoLevel.WARN, p, msg);
    }
    public void warn(@NotNull Player p, @NotNull Component msg) {
        sendMessage(InfoLevel.WARN, p, msg);
    }
    public void info(@NotNull Player p, @NotNull String msg) {
        sendMessage(InfoLevel.INFO, p, msg);
    }
    public void info(@NotNull Player p, @NotNull Component msg) {
        sendMessage(InfoLevel.INFO, p, msg);
    }

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull Component message) {
        Component txt = ColorUtils.chat(level.Prefix() + "").appendSpace().append(message);
        p.sendMessage(txt);
        p.playSound(p, level.Sound(), 1.0f, 1.0f);
        mobileProcess(p, txt);
    }

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull String message) {
        Component txt = ColorUtils.chat(level.Prefix() + " " + message);
        p.sendMessage(txt);
        p.playSound(p, level.Sound(), 1.0f, 1.0f);
        mobileProcess(p, txt);
    }

    private void mobileProcess(@NotNull Player p, @NotNull Component msg) {
        var user = UserManager.INSTANCE.getUser(p.getUniqueId());
        if (user == null) {
            ServerTransfer.sendReloadNotice(p);
            return;
        }
        if (user.isMobile()) p.sendActionBar(msg);
    }
}
