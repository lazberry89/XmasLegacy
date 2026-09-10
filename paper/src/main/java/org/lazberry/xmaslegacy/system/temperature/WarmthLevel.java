package org.lazberry.xmaslegacy.system.temperature;

import lombok.Getter;

@Getter
public enum WarmthLevel {
    WARM(2, 3),
    HIGH(1.5, 2),
    AVERAGE(1, 1),
    LOW(0.5, 0);

    private final double additionalRegeneration;
    private final int additionalWarmth;

    WarmthLevel(double additionalRegeneration, int additionalWarmth) {
        this.additionalRegeneration = additionalRegeneration;
        this.additionalWarmth = additionalWarmth;
    }
}
