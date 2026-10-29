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
     * Skript ItemAdder/Nexo 호환 채팅 배경 + 아이콘 접두사 빌더.
     *
     * 렌더링 순서 (Skript {@code {@1}<#color>{@2}} 원리와 100% 동일):
     *  1. OFFSET_MINUS_2     (-2px 미세 보정)
     *  2. <!shadow>          그림자 제거 (배경 렌더링 깔끔화)
     *  3. <#color>           레벨 색상으로 배경 tint
     *  4. CHAT_BACKGROUND    배경 이미지 글리프 (~218px 너비)
     *  5. OFFSET_MINUS_218   (-218px 커서 복귀, 텍스트가 배경 위에 겹쳐짐)
     *  6. <reset>            색상 리셋 (배경 색상이 텍스트로 번지는 것 완전 차단)
     *  7. [ icon ]           흰색 대괄호로 감싼 아이콘
     *
     * @return MiniMessage 형식의 접두사 문자열
     */
    public @NotNull String glyphPrefix() {
        return InfoUtils.OFFSET_MINUS_2
             + "<!shadow>"
             + "<" + hexColor + ">" + InfoUtils.CHAT_BACKGROUND
             + InfoUtils.OFFSET_MINUS_218
             + "<reset> "
             + "<" + hexColor + ">[ <white>" + icon + "</white><" + hexColor + "> ]<reset> ";
    }
}
