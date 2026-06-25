package xmaslegacy.Utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Utility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;

@Slf4j
@UtilityClass
public final class InfoUtils {

    public @Utility void info(@NotNull Player p, @NotNull String message) {
        sendMessage(InfoLevel.INFO, p, message);
    }
    public @Utility void warn(@NotNull Player p, @NotNull String message) {
        sendMessage(InfoLevel.WARN, p, message);
    }
    public @Utility void error(@NotNull Player p, @NotNull String message) {
        sendMessage(InfoLevel.ERROR, p, message);
    }
    public @Utility void error(@NotNull Player p, @NotNull String message, @NotNull Throwable e) {
        sendMessage(InfoLevel.ERROR, p, message);
        traceException(e);
    }
    public @Utility void info(@NotNull Player p, @NotNull Component component) {
        sendMessage(InfoLevel.INFO, p, component);
    }
    public @Utility void warn(@NotNull Player p, @NotNull Component component) {
        sendMessage(InfoLevel.WARN, p, component);
    }
    public @Utility void error(@NotNull Player p, @NotNull Component component) {
        sendMessage(InfoLevel.ERROR, p, component);
    }
    public @Utility void error(@NotNull Player p, @NotNull Component component, @NotNull Throwable e) {
        sendMessage(InfoLevel.ERROR, p, component);
        traceException(e);
    }

    public void traceException(@NotNull Throwable e) {
        log.error("Cause : {}\n\n Full Message : {}", e.getCause(), e.getMessage());
    }

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull Component message) {
        Component txt = level.prefix().comp().appendSpace().append(message);
        p.sendMessage(txt);
        p.playSound(p, level.sound(), 1.0f, 1.0f);
        mobileProcess(p, txt);
    }

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull String message) {
        Component txt = ColorUtils.chat(level.prefix() + " " + message);
        p.sendMessage(txt);
        p.playSound(p, level.sound(), 1.0f, 1.0f);
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
