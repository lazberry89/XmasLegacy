package org.lazberry.xmaslegacy.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Volatile {
    private static final Object LOCK = new Object();

    public static <V> boolean volatileSafeRunOrElse(
            @NotNull Supplier<V> valueSupplier,
            @NotNull Consumer<V> accept,
            @NotNull Runnable fail
    ) {
        V value = valueSupplier.get();
        if (value != null) {
            synchronized (LOCK) {
                value = valueSupplier.get();
                if (value != null) {
                    accept.accept(value);
                    return true;
                }
            }
        }

        fail.run();
        return false;
    }

    public static <V> boolean volatileSafeRun(@NotNull Supplier<V> valueSupplier, @NotNull Consumer<V> accept) {
        return volatileSafeRunOrElse(valueSupplier, accept, () -> {});
    }
}