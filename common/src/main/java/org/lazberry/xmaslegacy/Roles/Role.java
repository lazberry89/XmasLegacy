package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Skill;

import java.util.List;

public interface Role {
    String getKor();
    String name();
    @Nullable Role parent();
    int getTier();
    @NotNull List<Role> next();
    Skill bindTarget();
    List<Skill> bindRange();
    int getDashCount();

    static Role valueOf(@NotNull String name) throws IllegalArgumentException {
        try { return BasicRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        try { return SecondaryRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        try { return ThirdRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        try { return HiddenRoles.valueOf(name); } catch (IllegalArgumentException ignored) {}
        throw new IllegalArgumentException("No Role constant found with name: " + name);
    }
}
