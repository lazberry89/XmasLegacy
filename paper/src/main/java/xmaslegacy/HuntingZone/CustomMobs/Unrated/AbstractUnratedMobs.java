package xmaslegacy.HuntingZone.CustomMobs.Unrated;

import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.HuntingZone.CustomMobs.MobGrade;
import xmaslegacy.XmasLegacy;

public abstract class AbstractUnratedMobs implements CustomMob, UnratedMob {
    protected final @NotNull XmasLegacy plugin;
    protected final @NotNull MobGrade grade;

    public AbstractUnratedMobs() {
        this.plugin = XmasLegacy.getInstance();
        this.grade = MobGrade.UNRATED;
    }

    @Override
    public boolean isEntity(LivingEntity e) {
        String value = e.getPersistentDataContainer().get(KeyUtils.get("custom_mobs"), PersistentDataType.STRING);
        return value != null && value.equals(getKey().name());
    }

    @Override
    public @NotNull MobGrade getGrade() {
        return this.grade;
    }
}
