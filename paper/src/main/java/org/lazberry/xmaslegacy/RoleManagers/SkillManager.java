package org.lazberry.xmaslegacy.RoleManagers;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum SkillManager {
    INSTANCE;

    private final @NotNull Map<SkillSet, Skills<? extends RoleContainer>> skillMap = new HashMap<>();

    public void register(@NotNull PlayerSkills skill, @NotNull Skills<? extends RoleContainer> clazz) {
        this.skillMap.put(skill.origin(), clazz);
    }

    public int count() {
        return this.skillMap.size();
    }

    @SuppressWarnings("unchecked")
    public @NotNull <T extends RoleContainer> Skills<T> get(@NotNull SkillSet skill) {
        return Objects.requireNonNull((Skills<T>) this.skillMap.get(skill), "Skill is not registered! " + skill.getSkillName());
    }
}
