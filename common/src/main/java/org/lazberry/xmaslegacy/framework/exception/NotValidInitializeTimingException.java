package org.lazberry.xmaslegacy.framework.exception;

import org.jetbrains.annotations.NotNull;

public class NotValidInitializeTimingException extends RuntimeException {
    public NotValidInitializeTimingException(@NotNull String message) {
        super(message);
    }
}
