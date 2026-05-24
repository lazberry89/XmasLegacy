package xmasLegacy.ServerPrefix;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;

public class PrefixInterface implements InventoryHolder {
	private final Inventory inv;
	private final XmasLegacy plugin;
	private final PrefixManager PFM;
	private final UserManager UM;
	private final int MAX_SLOTS = 45;
	private final Map<Integer, ServerPrefix> slotMap = new HashMap<>();
	private int page = 0;
	private int MAX_PAGE = 0;

	public PrefixInterface(@NotNull Player p) {
		this.plugin = XmasLegacy.getInstance();
		this.PFM = PrefixManager.getInstance();
		this.UM = UserManager.getInstance();

		this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat("&c&l칭호관리"));
		for (int i = 45; i < 54; i++) this.inv.setItem(i, bg());
		this.inv.setItem(47, previous());
		this.inv.setItem(49, close());
		this.inv.setItem(51, next());

		update(p);
	}

	public void update(@NotNull Player p) {
		for (int i = 0; i < MAX_SLOTS; i++) inv.setItem(i, null);
		this.slotMap.clear();
		User user = UM.getUser(p.getUniqueId());
		if (user == null) return;

		var allPrefixes = user.getAvailablePrefix();

		// 정교한 최대 페이지 계산 (칭호가 0개일 때 0페이지가 되는 것도 방지)
		if (allPrefixes.isEmpty()) {
			this.MAX_PAGE = 1;
		} else {
			this.MAX_PAGE = (int) Math.ceil((double) allPrefixes.size() / MAX_SLOTS);
		}

		ServerPrefix equippedPrefix = user.getEquipPrefix();

		int start = page * MAX_SLOTS;
		int end = Math.min(start + MAX_SLOTS, allPrefixes.size());

		for (int i = start; i < end; i++) {
			var prefix = allPrefixes.get(i);
			boolean isEquipped = (equippedPrefix != null && equippedPrefix.equals(prefix));
			int currentSlot = i - start;

			ItemBuilder builder = ItemBuilder.of(plugin, Material.NAME_TAG)
					.setName(prefix.prefix())
					.setGlint(isEquipped);

			if (isEquipped) {
				builder.setLore(ColorUtils.chat("&a▶ 현재 장착 중"), ColorUtils.chat("&7클릭하여 해제"));
			} else {
				builder.setLore(ColorUtils.chat("&7클릭하여 장착"));
			}
			builder.build();

			this.slotMap.put(currentSlot, prefix);
			inv.setItem(currentSlot, builder.build());
		}
	}

	public @Nullable ServerPrefix getPrefix(int slot) {
		return this.slotMap.get(slot);
	}

	@Contract(pure = true)
	public void nextPage(@NotNull Player p) {
		if (this.page + 1 < this.MAX_PAGE) {
			this.page++;
			update(p);
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			p.updateInventory();
		} else {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
		}
	}

	@Contract(pure = true)
	public void prevPage(@NotNull Player p) {
		if (this.page > 0) {
			this.page--;
			update(p);
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			p.updateInventory();
		} else {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
		}
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}

	private @NotNull ItemStack previous() {
		return ItemBuilder.of(plugin, Material.ARROW)
				.setName(ColorUtils.chat("&a&l이전 페이지"))
				.setLore(ColorUtils.chat("&7이전 페이지로 이동합니다."))
				.hideAllFlags()
				.build();
	}

	private @NotNull ItemStack next() {
		return ItemBuilder.of(plugin, Material.ARROW)
				.setName(ColorUtils.chat("&a&l다음 페이지"))
				.setLore(ColorUtils.chat("&7다음 페이지로 이동합니다."))
				.hideAllFlags()
				.build();
	}

	private @NotNull ItemStack close() {
		return ItemBuilder.of(plugin, Material.BARRIER)
				.setName(ColorUtils.chat("&c&l닫기"))
				.setLore(ColorUtils.chat("&7창을 닫습니다."))
				.hideAllFlags()
				.build();
	}

	private @NotNull ItemStack bg() {
		return ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
				.setName(ColorUtils.chat(""))
				.setLore(ColorUtils.chat(""))
				.hideAllFlags()
				.build().clone();
	}
}
