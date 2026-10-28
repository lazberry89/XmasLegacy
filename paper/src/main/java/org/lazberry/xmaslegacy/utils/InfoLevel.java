package org.lazberry.xmaslegacy.utils;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Alert;

public enum InfoLevel {
    ERROR(Alert.RED,    Sound.BLOCK_NOTE_BLOCK_BASS,       "#ed5c5c", InfoUtils.ICON_DANGER),
    WARN (Alert.YELLOW, Sound.BLOCK_NOTE_BLOCK_BASS,       "#f5a623", InfoUtils.ICON_WARNING),
    INFO (Alert.GREEN,  Sound.ENTITY_ARROW_HIT_PLAYER,     "#5cedad", InfoUtils.ICON_BELL);

    private final @NotNull Alert  prefix;
    private final @NotNull Sound  sound;
    private final @NotNull String hexColor;
    private final @NotNull String icon;

    InfoLevel(@NotNull Alert prefix, @NotNull Sound sound, @NotNull String hexColor, @NotNull String icon) {
        this.prefix   = prefix;
        this.sound    = sound;
        this.hexColor = hexColor;
        this.icon     = icon;
    }

    public @NotNull Alert prefix() {
        return this.prefix;
    }

    public @NotNull Sound sound() {
        return this.sound;
    }

    public @NotNull String hexColor() {
        return this.hexColor;
    }

    public @NotNull String icon() {
        return this.icon;
    }

    /**
     * 배경 글리프(pluget:chat_background 폰트, 레벨 색상 tint) + 아이콘(흰색) + reset
     * 구조: <#color><bg_char></font><white>[ icon ]<reset>
     * - 배경 텍스처는 레벨 색상으로 tint되어 배경처럼 보임
     * - 아이콘과 텍스트는 색상 전파 없이 분리됨
     *
     * @return MiniMessage 형식의 접두사 문자열
     */
    public @NotNull String glyphPrefix() {
        // 배경 글리프: 레벨 색상으로 tint
        // 아이콘: white 고정 (색상 전파 차단)
        return "<" + hexColor + ">" + InfoUtils.CHAT_BACKGROUND
             + "<white>[ " + icon + " ]<reset>";
    }
}

