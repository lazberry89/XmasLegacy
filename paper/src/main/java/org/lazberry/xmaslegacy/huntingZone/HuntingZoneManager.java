package org.lazberry.xmaslegacy.huntingZone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.huntingZone.CustomMobs.MobRepository;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

@Registry.Include(type = ServerType.HUNTING)
public class HuntingZoneManager implements Initiator {
	private final @NotNull Map<ZoneType, HuntingZone> zones = new EnumMap<>(ZoneType.class);
	private final @NotNull MobRepository mobRepository;

	@Inject
	public HuntingZoneManager(@NotNull MobRepository mobRepository) {
		this.mobRepository = mobRepository;
	}

	public void init() {
		this.zones.put(ZoneType.ICE_STAGE, new HuntingZone(ZoneType.ICE_STAGE, "world", mobRepository));
		this.zones.put(ZoneType.SKY_GARDEN, new HuntingZone(ZoneType.SKY_GARDEN, "world", mobRepository));
		this.zones.put(ZoneType.SOUL_GRAVEYARD, new HuntingZone(ZoneType.SOUL_GRAVEYARD, "world", mobRepository));
	}

	public @Nullable HuntingZone getZone(@NotNull String value) {
		ZoneType type;
		try {
			type = ZoneType.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
		return this.getZone(type);
	}

	public @NotNull HuntingZone getZone(@NotNull ZoneType type) {
		return this.zones.get(type);
	}

	public @NotNull Collection<HuntingZone> getZones() {
		return this.zones.values();
	}
}
