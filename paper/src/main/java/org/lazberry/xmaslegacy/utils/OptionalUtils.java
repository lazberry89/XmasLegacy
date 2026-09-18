package org.lazberry.xmaslegacy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class OptionalUtils {

    public static <T> boolean ifNotNullOrElse(
            @Nullable T value,
            Consumer<? super @NotNull T> onPresent,
            Runnable onEmpty
    ) {
        Objects.requireNonNull(onPresent, "onPresent consumer must not be null");
        Objects.requireNonNull(onEmpty, "onEmpty runnable must not be null");

        if (value != null) {
            onPresent.accept(value);
            return true;
        }
        else {
            onEmpty.run();
            return false;
        }
    }

    public static <T, U> boolean allNotNullOrElse(
            @Nullable T value1, @Nullable U value2,
            BiConsumer<? super @NotNull T, ? super @NotNull U> onPresent,
            Runnable onEmpty) {
        Objects.requireNonNull(onPresent, "onPresent consumer must not be null");
        Objects.requireNonNull(onEmpty, "onEmpty runnable must not be null");

        if (value1 != null && value2 != null) {
            onPresent.accept(value1, value2);
            return true;
        }
        else {
            onEmpty.run();
            return false;
        }
    }

    public static <T> boolean ifNotNull(
            @Nullable T value,
            Consumer<? super @NotNull T> onPresent
    ) {
        if (value != null) {
            onPresent.accept(value);
            return true;
        }
        return false;
    }
}
