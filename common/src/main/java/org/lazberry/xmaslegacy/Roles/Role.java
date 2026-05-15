package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Role {
    String getKor();
    String name();
    @Nullable Role parent();
    int getTier();
    @NotNull List<Role> next();
}
