package xmasLegacy.PlayerUtils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.XmasLegacy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum BagManager {
	INSTANCE;

	private final @NotNull Map<UUID, TempBag> bags = new HashMap<>();
	private final @NotNull XmasLegacy plugin;

	BagManager() {
		this.plugin = XmasLegacy.getInstance();
	}

	public @NotNull TempBag getUserBags(Player p) {
		return bags.computeIfAbsent(p.getUniqueId(),
				uuid -> new TempBag(plugin, uuid));
	}

	public @NotNull TempBag getBag(UUID uuid) {
		return bags.computeIfAbsent(uuid, k -> new TempBag(plugin, k));
	}

    @CanIgnoreReturnValue
	public List<ItemStack> addItem(Player p, ItemStack item, int amount) {
		ItemStack clone = item.clone();
		List<ItemStack> result = getUserBags(p).addItem(clone, amount);
		if (!result.isEmpty()) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 가방이 가득 찼습니다!"));
            result.forEach(s -> p.getWorld().dropItemNaturally(p.getLocation(), s));
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.0f);
		} else {
			p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 가방에 아이템이 추가되었습니다!"));
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		}
        return result;
	}
    @Contract(pure = true)
	public ItemStack[] getPlayerBag(@NotNull Player p) {
		return getUserBags(p).getInventory().getContents();
	}

	public void saveAllBags() {
		File file = new File(plugin.getDataFolder(), "bags.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (Map.Entry<UUID, TempBag> entry : bags.entrySet()) {
			UUID uuid = entry.getKey();
			// 가방의 인벤토리 내용물(ItemStack[])을 가져옴
			ItemStack[] contents = entry.getValue().getInventory().getContents();

			// YAML에 UUID를 키로 하여 저장
			config.set(uuid.toString(), contents);
		}

		try {
			config.save(file);
		} catch (IOException e) {
			plugin.getSLF4JLogger().error("가방 정보를 저장하던 중 오류: {}", e.getMessage(), e);
		}
	}
	@SuppressWarnings("SuspiciousToArrayCall")
	public void loadAllBags() {
		File file = new File(plugin.getDataFolder(), "bags.yml");
		if (!file.exists()) return;

		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (String uuidStr : config.getKeys(false)) {
			UUID uuid = UUID.fromString(uuidStr);
			List<?> list = config.getList(uuidStr);
			if (list == null) continue;

			ItemStack[] contents = list.toArray(new ItemStack[0]);

			TempBag bag = new TempBag(plugin, uuid);
			bag.getInventory().clear();
			bag.getInventory().setContents(contents);

			bags.put(uuid, bag);
		}
	}
}
