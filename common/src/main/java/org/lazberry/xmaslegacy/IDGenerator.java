package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ThreadLocalRandom;

public class IDGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static @NotNull String generateRandomId() {
        StringBuilder sb = new StringBuilder(Constants.ID_LENGTH);

        for (int i = 0; i < Constants.ID_LENGTH; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}
