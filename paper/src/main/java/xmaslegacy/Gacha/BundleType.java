package xmaslegacy.Gacha;

import lombok.Getter;

@Getter
public enum BundleType {
    NORMAL("일반 치장"),
    HIGH_END("고급 치장"),
    CHROMATIC_BUNDLE("크로마틱 번들"),
    CHROMATIC_BOX("크로마틱 히든");

    private final String kor;

    BundleType(String kor) {
        this.kor = kor;
    }

}