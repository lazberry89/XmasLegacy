package xmaslegacy.HuntingZone;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public enum HuntingZoneManager {
	INSTANCE;

	private final @NotNull Map<ZoneType, HuntingZone> zones = new EnumMap<>(ZoneType.class);

	HuntingZoneManager() {}

	public void init() {
		this.zones.put(ZoneType.ICE_STAGE, new HuntingZone(ZoneType.ICE_STAGE, "world"));
		this.zones.put(ZoneType.SKY_GARDEN, new HuntingZone(ZoneType.SKY_GARDEN, "world"));
		this.zones.put(ZoneType.SOUL_GRAVEYARD, new HuntingZone(ZoneType.SOUL_GRAVEYARD, "world"));
	}

	public @NotNull HuntingZone getZone(@NotNull ZoneType type) {
		return this.zones.get(type);
	}

	public @NotNull Collection<HuntingZone> getZones() {
		return this.zones.values();
	}
}
