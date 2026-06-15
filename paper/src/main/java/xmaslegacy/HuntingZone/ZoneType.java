package xmaslegacy.HuntingZone;

import xmaslegacy.HuntingZone.CustomMobs.MobKey;

public enum ZoneType {
	ICE_STAGE(MobKey.ICE_CUBE, MobKey.ICED_ZOMBIE, MobKey.HUNTER_ZOMBIE),
	SKY_GARDEN(),
	SOUL_GRAVEYARD();

	private final MobKey[] mobs;

	ZoneType(MobKey... mobs) {
		this.mobs = mobs;
	}

	public MobKey[] getMobs() {
		return this.mobs;
	}
}
