package org.lazberry.xmaslegacy.settings.Framework.FrameworkExceptions;

import org.jetbrains.annotations.NotNull;

public class NotValidInitializeTimingException extends RuntimeException {
    public NotValidInitializeTimingException(@NotNull String message) {
        super(message);
    }
}
