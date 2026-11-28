package org.lazberry.xmaslegacy.bags;

import org.jetbrains.annotations.Nullable;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Components.Exclude(type = ServerType.LOBBY)
public class BagManager implements Initiator {
	private final Map<UUID, Bag> bags = new ConcurrentHashMap<>();

	public BagManager() {}

	@Override
	public void init() {
		// bags는 플레이어 접속 시 loadBag로 로드되므로 여기서 초기화하지 않음
	}

	public void setUserBag(UUID uuid, Bag bag) {
		bags.put(uuid, bag);
	}

	public void removeUserBag(UUID uuid) {
		bags.remove(uuid);
	}

	/** True once this server holds the player's real bag (getBag would otherwise create an empty one). */
	public boolean isLoaded(UUID uuid) {
		return bags.containsKey(uuid);
	}

	/**
	 * null until the real bag is loaded. Creating an empty one instead would be saved over the real bag,
	 * and items put into it would vanish as soon as the real one arrives.
	 */
	public @Nullable Bag getBag(UUID uuid) {
		return bags.get(uuid);
	}

	/** @return what did not fit; everything if the bag is not loaded. */
	private List<ItemStack> addToBag(UUID uuid, ItemStack item) {
		Bag bag = getBag(uuid);
		return bag == null ? List.of(item.clone()) : bag.addItem(item.clone());
	}

	public void addItem(Player p, ItemStack item) {
		List<ItemStack> result = addToBag(p.getUniqueId(), item);
		if (!result.isEmpty()) {
            result.forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.0f);
		} else {
			InfoUtils.info(p, "가방에 아이템이 추가되었습니다.");
		}
	}



	public void addAll(Player p, Collection<ItemStack> items) {
		if (items.isEmpty()) return;

		var uuid = p.getUniqueId();
		for (var item : items) {
			List<ItemStack> remain = addToBag(uuid, item);
			remain.forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
		}
	}

	public List<ItemStack> getBagContents(UUID uuid) {
		Bag bag = getBag(uuid);
		if (bag == null) return Collections.emptyList();
		ItemStack[] contents = bag.getContents();

		return Arrays.stream(contents)
				.filter(item -> item != null && !item.getType().isAir())
				.collect(Collectors.toList());
	}

	public void clear() {
		bags.clear();
	}

	@Override
	public void close() {
		// Do not clear bags here to prevent data loss before BagSaveManager.close() persists data to DB/Redis.
	}
}
