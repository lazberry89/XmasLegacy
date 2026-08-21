package org.lazberry.xmaslegacy.huntingZone.CustomMobs.Unrated;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.huntingZone.CustomMobs.CustomMob;
import org.lazberry.xmaslegacy.huntingZone.CustomMobs.MobGrade;
import org.lazberry.xmaslegacy.XmasLegacy;

public abstract class AbstractUnratedMobs implements CustomMob, UnratedMob {
    protected final @NotNull XmasLegacy plugin;
    protected final @NotNull MobGrade grade;

    public AbstractUnratedMobs() {
        this.plugin = XmasLegacy.getInstance();
        this.grade = MobGrade.UNRATED;
    }

    @Override
    public @NotNull MobGrade getGrade() {
        return this.grade;
    }
}
