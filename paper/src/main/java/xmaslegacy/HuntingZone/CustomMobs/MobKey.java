package xmaslegacy.HuntingZone.CustomMobs;

import org.jetbrains.annotations.NotNull;
import xmaslegacy.HuntingZone.CustomMobs.Unrated.CustomMob;
import xmaslegacy.HuntingZone.CustomMobs.Unrated.HunterZombie;
import xmaslegacy.HuntingZone.CustomMobs.Unrated.IcedZombie;
import xmaslegacy.HuntingZone.ZoneType;

public enum MobKey {
    ICED_ZOMBIE(ZoneType.ICE_STAGE, IcedZombie.class),
    HUNTER_ZOMBIE(ZoneType.ICE_STAGE, HunterZombie.class),
    ICE_CUBE(ZoneType.ICE_STAGE, IcedZombie.class);

	private final ZoneType type;
    private final Class<? extends CustomMob> clazz;

    MobKey(ZoneType type, Class<? extends CustomMob> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

	public @NotNull ZoneType getType() {
		return this.type;
	}
    public @NotNull Class<? extends CustomMob> getClazz() {
        return this.clazz;
    }
}
