package org.lazberry.xmaslegacy.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;

@Slf4j
@UtilityClass
public final class InfoUtils {

    private static final String FONT_START = "<font:chat_background:default>";
    private static final String FONT_END   = "</font>";

    public static final String CHAT_BACKGROUND = FONT_START + "\uA40E" + FONT_END; // ꐎ  background.png
    public static final String CHAT_PREFIX     = FONT_START + "\uA40F" + FONT_END; // ꐏ  prefix.png

    public static final String ICON_BELL    = FONT_START + "\uA410" + FONT_END; // ꐐ  bell.png
    public static final String ICON_COIN    = FONT_START + "\uA411" + FONT_END; // ꐑ  coin.png
    public static final String ICON_DANGER  = FONT_START + "\uA412" + FONT_END; // ꐒ  danger.png
    public static final String ICON_DOWN    = FONT_START + "\uA413" + FONT_END; // ꐓ  down.png
    public static final String ICON_GIFT    = FONT_START + "\uA414" + FONT_END; // ꐔ  gift.png
    public static final String ICON_HEART   = FONT_START + "\uA415" + FONT_END; // ꐕ  heart.png
    public static final String ICON_UP      = FONT_START + "\uA416" + FONT_END; // ꐖ  up.png
    public static final String ICON_WARNING = FONT_START + "\uA417" + FONT_END; // ꐗ  warning.png

    // ── 커서 오프셋 (space provider, 유저 입력 불가 PUA) ──────
    // font JSON 의 "type":"space","advances":{"ꐌ":-2, "ꐍ":-218} 과 매칭
    // <shift:-N> 대신 사용 — MiniMessage 기본 태그가 아니라 직접 문자로 처리
    public static final String OFFSET_MINUS_2   = FONT_START + "\uA40C" + FONT_END;
    public static final String OFFSET_MINUS_218 = FONT_START + "\uA40D" + FONT_END;

    public static final class Glyphs {
        public static final String BACKGROUND = InfoUtils.CHAT_BACKGROUND;
        public static final String PREFIX     = InfoUtils.CHAT_PREFIX;
        public static final char CHAR_BACKGROUND = '\uA40E';
        public static final char CHAR_PREFIX     = '\uA40F';
    }

    public static final class Icons {
        public static final String BELL    = InfoUtils.ICON_BELL;
        public static final String COIN    = InfoUtils.ICON_COIN;
        public static final String DANGER  = InfoUtils.ICON_DANGER;
        public static final String DOWN    = InfoUtils.ICON_DOWN;
        public static final String GIFT    = InfoUtils.ICON_GIFT;
        public static final String HEART   = InfoUtils.ICON_HEART;
        public static final String UP      = InfoUtils.ICON_UP;
        public static final String WARNING = InfoUtils.ICON_WARNING;
    }

    public void info(@NotNull Player p, @NotNull String message, Object... args) {
        sendMessage(InfoLevel.INFO, p, message, args);
    }
    public void warn(@NotNull Player p, @NotNull String message, Object... args) {
        sendMessage(InfoLevel.WARN, p, message, args);
    }
    public void error(@NotNull Player p, @NotNull String message, Object... args) {
        sendMessage(InfoLevel.ERROR, p, message, args);
    }
    public void error(@NotNull Player p, @NotNull String message, @NotNull Throwable e, Object... args) {
        sendMessage(InfoLevel.ERROR, p, message, args);
        traceException(e);
    }
    public void info(@NotNull Player p, @NotNull Component component) {
        sendMessage(InfoLevel.INFO, p, component);
    }
    public void warn(@NotNull Player p, @NotNull Component component) {
        sendMessage(InfoLevel.WARN, p, component);
    }
    public void error(@NotNull Player p, @NotNull Component component) {
        sendMessage(InfoLevel.ERROR, p, component);
    }
    public void error(@NotNull Player p, @NotNull Component component, @NotNull Throwable e) {
        sendMessage(InfoLevel.ERROR, p, component);
        traceException(e);
    }

    public void traceException(@NotNull Throwable e) {
        log.error("Error occurred: ", e);
    }

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull Component message) {
        Component safeMsg = ColorUtils.minimessage("<white>").append(message);
        Component txt = buildGlyphPrefix(level).append(safeMsg);
        p.sendMessage(txt);
        p.playSound(p, level.sound(), 1.0f, 1.0f);
        mobileProcess(p, txt);
    }

    private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull String message, Object... args) {
        String formatted = MessageFormatter.arrayFormat(message, args).getMessage();
        switch (level) {
            case WARN  -> log.warn(formatted);
            case ERROR -> log.error(formatted);
            default    -> {}
        }
        Component content = parseMessage(formatted);
        Component safeContent = ColorUtils.minimessage("<white>").append(content);
        Component txt = buildGlyphPrefix(level).append(safeContent);
        p.sendMessage(txt);
        p.playSound(p, level.sound(), 1.0f, 1.0f);
        mobileProcess(p, txt);
    }

    private Component parseMessage(@NotNull String message) {
        if (message.isEmpty()) return Component.empty();
        String converted = convertLegacyToMiniMessage(message);
        return ColorUtils.minimessage(converted);
    }

    private String convertLegacyToMiniMessage(@NotNull String input) {
        if (!input.contains("&")) return input;
        return input
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obf>")
                .replace("&l", "<b>")
                .replace("&m", "<st>")
                .replace("&n", "<u>")
                .replace("&o", "<i>")
                .replace("&r", "<reset>");
    }

    private Component buildGlyphPrefix(@NotNull InfoLevel level) {
        return ColorUtils.minimessage(level.glyphPrefix());
    }

    private void mobileProcess(@NotNull Player p, @NotNull Component msg) {
        if (FloodgateUtils.isFloodgate(p.getUniqueId())) p.sendActionBar(msg);
    }
}
