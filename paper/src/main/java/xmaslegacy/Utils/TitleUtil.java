package xmaslegacy.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.ApiStatus;
import org.lazberry.xmaslegacy.ColorUtils;

import java.time.Duration;

public final class TitleUtil {

    @ApiStatus.Internal
    private TitleUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Adventure API의 Title 객체를 빌드하여 반환합니다.
     * @param title    메인 타이틀 문자열 (& 색상코드 지원, null 가능)
     * @param subtitle 서브 타이틀 문자열 (& 색상코드 지원, null 가능)
     * @param fadeIn   나타나는 시간 (틱 단위, 20틱 = 1초)
     * @param stay     유지 시간 (틱 단위)
     * @param fadeOut  사라지는 시간 (틱 단위)
     * @return 빌드 완료된 net.kyori.adventure.title.Title 객체
     */
    public static Title create(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component mainComponent = (title != null) ?
                ColorUtils.chat(title) : Component.empty();

        Component subComponent = (subtitle != null) ?
                ColorUtils.chat(subtitle) : Component.empty();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        return Title.title(mainComponent, subComponent, times);
    }

    /**
     * [오버로딩] 기본 시간(나타나기 0.5초 / 유지 2초 / 사라지기 0.5초)이 내장된 Title 객체를 반환합니다.
     */
    public static Title create(String title, String subtitle) {
        return create(title, subtitle, 10, 40, 10);
    }
}
