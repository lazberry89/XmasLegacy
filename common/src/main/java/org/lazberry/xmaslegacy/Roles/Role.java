package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Role {

    /**
     * Each role enum must have its own Korean name to Use as In-game UI/UX.
     * @return Korean name of each Role.
     */
    @NotNull String getKor();

    /**
     * essential method of enum.
     * @return eng name of role.
     */
    @NotNull String name();

    @NotNull String description();

    /**
     * This interface is implemented by all role enums, so this method helps parse string to Role enum instance.
     * <pre>{@code
     * try {
     *      Role a = Role.valueOf("MAGE");
     * } catch (IllegalArgumentException e) {}
     * }</pre>
     * @param name wanted value
     * @return target value
     * @throws IllegalArgumentException throws IllegalArgumentException if fails.
     */
    @Contract(value = "_ -> !null", pure = true)
    static @NotNull Role valueOf(@NotNull String name) throws IllegalArgumentException {
        try { return ServerRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        try { return HiddenRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        throw new IllegalArgumentException("No Role constant found with name: " + name);
    }

    /**
     * Generic parsing method. Returns null when failed.
     * <pre>{@code
     * @Nullable Role a = Role.parseRole("a");
     * }</pre>
     * @param name wanted value
     * @return target value, null when failed.
     */
    @Contract(pure = true)
    static @Nullable Role parseRole(@NotNull String name) {
        return parseRole(name, null);
    }

    /**
     * Parsing method, if fails, it won't throw Exception but returns default value.
     * <pre>{@code
     * @NotNull Role a = Role.parseRole("a", BasicRoles.MAGE);
     * }</pre>
     * @param name wanted value
     * @param def default value to use as failure return.
     * @return target value, default when failed.
     */
    @Contract(value = "_, !null -> !null; _, null -> _", pure = true)
    static Role parseRole(@NotNull String name, Role def) {
        Role result;
        try {
            result = valueOf(name);
        } catch (IllegalArgumentException e) {
            result = def;
        }
        return result;
    }
}
