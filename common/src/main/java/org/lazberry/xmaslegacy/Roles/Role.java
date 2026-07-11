package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.List;

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

    /**
     * Besides BasicRole(located lowest at Tree, don't have parent.), Each Role level has role before them.
     * @return role value of current role's parent.
     */
    @Nullable Role parent();

    /**
     * Indicated where the role locates in the Tree Structure.
     * @return int value that indicated where the role is.
     */
    int getTier();

    /**
     * Role system has Tree Structure. So this method indicates which role locates next from current.
     * @return Some role has a several selections to change role. So returns as list.
     */
    @NotNull List<Role> next();

    /**
     * Target emblem must, always binds a single skill.
     * @return bind skill of target emblem.
     */
    @NotNull SkillSet bindTarget();

    /**
     * Range emblem binds several skillSets if role implements Unpromotable interface.
     * Unpromotable role has 1 more skill, so range skill binds totally 2 skills.
     * @return Available skill List.
     */
    @NotNull List<SkillSet> bindRange();

    /**
     * Defines count of dash that each role can use.
     * @return count of dash that target role can use.
     */
    int getDashCount();

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
        try { return BasicRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        try { return SecondaryRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        try { return ThirdRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
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
    @Contract(value = "_ -> _", pure = true)
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
