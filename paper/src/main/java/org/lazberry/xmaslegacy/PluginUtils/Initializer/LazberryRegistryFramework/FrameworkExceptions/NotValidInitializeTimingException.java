package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions;

import org.jetbrains.annotations.NotNull;

public class NotValidInitializeTimingException extends RuntimeException {
    public NotValidInitializeTimingException(@NotNull String message) {
        super(message);
    }
}
