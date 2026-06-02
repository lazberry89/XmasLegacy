package xmasLegacy.HuntingZone.CustomMobs;

import java.util.Random;

public enum MobKey {
    ICED_ZOMBIE(200, 400),
    HUNTER_ZOMBIE(300, 350),
    ICE_CUBE(150, 250);

    private final int from;
    private final int to;
    private final Random random;

    MobKey(int from, int to) {
        this.from = from;
        this.to = to;
        this.random = new Random();
    }

    public int amount() {
        return random.nextInt(from, to);
    }
}
