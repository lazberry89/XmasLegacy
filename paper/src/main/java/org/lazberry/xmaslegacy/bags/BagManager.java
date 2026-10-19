package org.lazberry.xmaslegacy.bags;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Registry.Exclude(type = ServerType.LOBBY)
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

	public Bag getBag(UUID uuid) {
		return bags.computeIfAbsent(uuid, Bag::new);
	}

	public void addItem(Player p, ItemStack item) {
		ItemStack clone = item.clone();
		UUID uuid = p.getUniqueId();

		List<ItemStack> result = getBag(uuid).addItem(clone);
		if (!result.isEmpty()) {
            result.forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.0f);
		} else {
			InfoUtils.info(p, "가방에 아이템이 추가되었습니다.");
		}
	}

	public void addItem(UUID uuid, ItemStack item) {
		var bag = getBag(uuid);
		bag.addItem(item);
	}

	public void addAll(Player p, Collection<ItemStack> items) {
		if (items.isEmpty()) return;

		var uuid = p.getUniqueId();
		for (var item : items) {
			var cloned = item.clone();
			List<ItemStack> remain = getBag(uuid).addItem(cloned);
			remain.forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
		}
	}

	public List<ItemStack> getBagContents(UUID uuid) {
		ItemStack[] contents = getBag(uuid).getContents();
		if (contents == null) return Collections.emptyList();

		return Arrays.stream(contents)
				.filter(item -> item != null && !item.getType().isAir())
				.collect(Collectors.toList());
	}

	@Override
	public void close() {
		bags.clear();
	}
}
