package org.lazberry.xmaslegacy.blueprint.selection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintSelectionManager {
	private static final NamespacedKey key = KeyUtils.get("blueprint_selection");

	private final Map<UUID, Location> first = new HashMap<>();
	private final Map<UUID, Location> second = new HashMap<>();
	private final XmasLegacy plugin;

	@Inject
	public BlueprintSelectionManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public ItemStack tool() {
		return ItemBuilder.of(plugin, Material.DIAMOND_HOE)
				.setTag(key, PersistentDataType.BOOLEAN, true)
				.build();
	}

	public Optional<Location> getFirstSelection(UUID uuid) {
		return Optional.ofNullable(first.get(uuid));
	}

	public Optional<Location> getSecondSelection(UUID uuid) {
		return Optional.ofNullable(second.get(uuid));
	}

	public boolean isTool(ItemStack item) {
		return KeyUtils.hasKey(item, key, PersistentDataType.BOOLEAN, true);
	}

	public void addFirstSelection(UUID uuid, Location location) {
		first.put(uuid, location);
	}

	public void addSecondSelection(UUID uuid, Location location) {
		second.put(uuid, location);
	}

	public void clearSelection(UUID uuid) {
		first.remove(uuid);
		second.remove(uuid);
	}
}
