package org.lazberry.xmaslegacy.mining.logics;

import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Registry.Include(type = ServerType.MAIN)
public class MineCreateManager {
	private final Map<UUID, Location> firstSelection = new HashMap<>();
	private final Map<UUID, Location> secondSelection = new HashMap<>();

	public void addFirstSelection(UUID uuid, Location location) {
		firstSelection.put(uuid, location);
	}

	public Location getFirstSelection(UUID uuid) {
		return firstSelection.get(uuid);
	}

	public Location getSecondSelection(UUID uuid) {
		return secondSelection.get(uuid);
	}

	public void addSecondSelection(UUID uuid, Location location) {
		secondSelection.put(uuid, location);
	}

	public void clearSelection(UUID uuid) {
		firstSelection.remove(uuid);
		secondSelection.remove(uuid);
	}
}
