package xmasLegacy.HuntingZone.CustomMobs;

import xmasLegacy.HuntingZone.ZoneType;

public enum MobKey {
    ICED_ZOMBIE(ZoneType.ICE_STAGE),
    HUNTER_ZOMBIE(ZoneType.ICE_STAGE),
    ICE_CUBE(ZoneType.ICE_STAGE);

	private final ZoneType type;

    MobKey(ZoneType type) {
        this.type = type;
    }

	public  ZoneType getType() {
		return this.type;
	}
}
