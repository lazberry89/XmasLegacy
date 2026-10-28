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

    // ===== 리소스팩 배경/아이콘 글리프 (모두 pluget:chat_background 폰트로 격리) =====
    // Oraxen 기본 폰트와 char 충돌 방지를 위해 전용 font 네임스페이스 사용
    private static final String FONT_START = "<font:pluget:chat_background>";
    private static final String FONT_END   = "</font>";

    public static final String CHAT_BACKGROUND = FONT_START + "\uA40E" + FONT_END; // ꐎ
    public static final String CHAT_PREFIX     = FONT_START + "\uA40F" + FONT_END; // ꐏ

    public static final String ICON_BELL    = FONT_START + "\uA410" + FONT_END; // ꐐ
    public static final String ICON_COIN    = FONT_START + "\uA411" + FONT_END; // ꐑ
    public static final String ICON_DANGER  = FONT_START + "\uA412" + FONT_END; // ꐒ
    public static final String ICON_DOWN    = FONT_START + "\uA413" + FONT_END; // ꐓ
    public static final String ICON_GIFT    = FONT_START + "\uA414" + FONT_END; // ꐔ
    public static final String ICON_HEART   = FONT_START + "\uA415" + FONT_END; // ꐕ
    public static final String ICON_UP      = FONT_START + "\uA416" + FONT_END; // ꐖ
    public static final String ICON_WARNING = FONT_START + "\uA417" + FONT_END; // ꐗ


    /**
     * 리소스팩 배경 및 글리프 네임스페이스 상수
     */
    public static final class Glyphs {
        public static final String BACKGROUND = InfoUtils.CHAT_BACKGROUND;
        public static final String PREFIX     = InfoUtils.CHAT_PREFIX;
        public static final char CHAR_BACKGROUND = 'ꐎ';
        public static final char CHAR_PREFIX     = 'ꐏ';
    }

    /**
     * Oraxen 리소스팩 아이콘 글리프 네임스페이스 상수
     */
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
        Component txt = buildGlyphPrefix(level).appendSpace().append(message);
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
        Component txt = buildGlyphPrefix(level).appendSpace().append(content);
        p.sendMessage(txt);
        p.playSound(p, level.sound(), 1.0f, 1.0f);
        mobileProcess(p, txt);
    }

    /**
     * 레거시 색상 코드(&)와 MiniMessage 태그를 모두 지원하기 위한 파서
     */
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

    /**
     * Skript {@code {@1}<#color>{@2}} 패턴을 Java로 구현한 벨퍼 함수.
     * 리소스팩 {@code pluget:chat_background} 폰트 글리프로
     * 레벨 색상 배경을 렌더링한 후 커서를 복귀합니다.
     *
     * @param level 메시지 레벨 (ERROR / WARN / INFO)
     * @return 배경 글리프가 포함된 MiniMessage Component
     */
    private Component buildGlyphPrefix(@NotNull InfoLevel level) {
        return ColorUtils.minimessage(level.glyphPrefix());
    }

    private void mobileProcess(@NotNull Player p, @NotNull Component msg) {
        if (FloodgateUtils.isFloodgate(p.getUniqueId())) p.sendActionBar(msg);
    }
}
