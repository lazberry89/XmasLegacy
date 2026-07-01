package org.lazberry.xmaslegacy.Utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Utility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;
import org.slf4j.helpers.MessageFormatter;

@Slf4j
@UtilityClass
public final class InfoUtils {

    public @Utility void info(@NotNull Player p, @NotNull String message, Object... args) {
        sendMessage(InfoLevel.INFO, p, message, args);
    }
    public @Utility void warn(@NotNull Player p, @NotNull String message, Object... args) {
        sendMessage(InfoLevel.WARN, p, message, args);
    }
    public @Utility void error(@NotNull Player p, @NotNull String message, Object... args) {
        sendMessage(InfoLevel.ERROR, p, message, args);
    }
    public @Utility void error(@NotNull Player p, @NotNull String message, @NotNull Throwable e, Object... args) {
        sendMessage(InfoLevel.ERROR, p, message, args);
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

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull String message, Object... args) {
        String formattedMessage = MessageFormatter.arrayFormat(message, args).getMessage();
        switch (level) {
            case INFO -> log.info(message, args);
            case WARN -> log.warn(message, args);
            case ERROR -> log.error(message, args);
        }
        Component txt = ColorUtils.chat(level.prefix() + " " + formattedMessage);
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
