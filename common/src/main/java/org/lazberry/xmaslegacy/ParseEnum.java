package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParseEnum {
    private final @NotNull Class<? extends Enum<?>> clazz;

    @ApiStatus.Internal
    private ParseEnum(@NotNull Class<? extends Enum<?>> enumClass) {
        this.clazz = enumClass;
    }

    /**
     * static method to get instance of Utility class.
     * <pre>{@code
     * ParseEnum.of(Enum.class).parse() //Enabled methods
     * }</pre>
     * @param enumClass instance of enum class to parse
     * @return Parsing Utility class instance.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ParseEnum of(Class<? extends Enum<?>> enumClass) {
        return new ParseEnum(enumClass);
    }

    /**
     * Run a process of parsing arg1 string to target Enum instance value.
     * <pre>{@code
     * EntityType type = ParseEnum.of(EntityType.class).parse("ZOMBIE");
     * }</pre>
     * @param value wanted value
     * @return return value of enum instance, if fail returns null.
     * @param <E> enum
     */
    @Contract("null -> null")
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> @Nullable E parse(@Nullable String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Enum.valueOf((Class<E>) clazz, value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Runs a parsing process, returns default value if failed.
     * <pre>{@code
     * Material material = ParseEnum.of(Material.class).parseOrdefault("NETHERITE", Material.NETHERITE_SWORD); //returns netherite
     * }</pre>
     * @param value target value to parse
     * @param def default value to use if failed.
     * @return target enum instance, returns default if fails.
     * @param <E> enum
     */
    @Contract("null, _ -> param2; _, _ -> param2")
    public <E extends Enum<E>> @NotNull E parseOrDefault(@Nullable String value, @NotNull E def) {
        E result = parse(value);
        return result != null ? result : def;
    }
}
