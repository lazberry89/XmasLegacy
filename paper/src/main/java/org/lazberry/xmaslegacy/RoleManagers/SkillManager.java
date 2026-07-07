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

    /**
     * Only called by Reflection. Used as mapping skill enum to skill instance.
     * @param skill Skill enum
     * @param clazz target class that implements Skills interface
     */
    public void register(@NotNull PlayerSkills skill, @NotNull Skills<? extends RoleContainer> clazz) {
        this.skillMap.put(skill.origin(), clazz);
    }

    /**
     * If this value is not same as Enum of Player Skills, Maybe something went wrong.
     * @return registered mapped skills' size(count).
     */
    public int count() {
        return this.skillMap.size();
    }

    @SuppressWarnings("unchecked")
    public @NotNull <T extends RoleContainer> Skills<T> get(@NotNull SkillSet skill) {
        return Objects.requireNonNull((Skills<T>) this.skillMap.get(skill), "Skill is not registered! " + skill.getSkillName());
    }

    public @NotNull <S extends Skills<?>> S get(@NotNull SkillSet skill, @NotNull Class<S> clazz) {
        Object registeredSkill = this.skillMap.get(skill);
        return Objects.requireNonNull(clazz.cast(registeredSkill), "스킬이 등록되지 않았거나 타입이 일치하지 않습니다! " + skill.getSkillName());
    }
}
