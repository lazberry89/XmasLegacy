package xmasLegacy.HuntingZone;

import java.util.HashSet;
import java.util.Set;

public class HuntingZoneManager {
	private final Set<HuntingZone> zones = new HashSet<>(3);
	private static HuntingZoneManager instance;

	public static HuntingZoneManager getInstance() {
		if (instance == null) instance = new HuntingZoneManager();
		return instance;
	}

	private HuntingZoneManager() {}

	public void init() {
		this.zones.add(new HuntingZone(ZoneType.ICE_STAGE, "world"));
		this.zones.add(new HuntingZone(ZoneType.SKY_GARDEN, "world"));
		this.zones.add(new HuntingZone(ZoneType.SOUL_GRAVEYARD, "world"));
	}
}
