package org.lazberry.xmaslegacy.utils;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class OptionalUtils {

    public static <T> void ifNotNullOrElse(
            @Nullable T value,
            Consumer<? super T> onPresent,
            Runnable onEmpty
    ) {
        Objects.requireNonNull(onPresent, "onPresent consumer must not be null");
        Objects.requireNonNull(onEmpty, "onEmpty runnable must not be null");

        if (value != null) onPresent.accept(value);
        else onEmpty.run();
    }
}
