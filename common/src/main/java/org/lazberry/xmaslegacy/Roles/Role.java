package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Role {
    String getKor();
    @Nullable Role parent();
    int getTier();
    List<Role> next();
}
