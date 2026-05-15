package xmasLegacy.ServerPrefix;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class PrefixInterface implements InventoryHolder {
	private final Inventory inv;
	private final XmasLegacy plugin;
	private final PrefixManager PFM;
	private final UserManager UM;
	private int page = 0;
	private final int MAX_SLOTS = 45;

	public PrefixInterface(XmasLegacy plugin, Player p) {
		this.plugin = plugin;
		this.PFM = plugin.PFM;
		this.UM = plugin.UM;

		this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat("&c&l칭호관리"));
		for (int i = 45; i < 54; i++) this.inv.setItem(i, bg());
		this.inv.setItem(47, previous());
		this.inv.setItem(49, close());
		this.inv.setItem(51, next());

		update(p);
	}

	public void update(Player p) {
		for (int i = 0; i < MAX_SLOTS; i++) inv.setItem(i, null);

		User user = UM.getUser(p.getUniqueId());
		if (user == null) return;

		var allPrefixes = user.getAvailablePrefix();
		ServerPrefix equippedPrefix = user.getEquipPrefix(); // 현재 장착 중인 칭호

		int start = page * MAX_SLOTS;
		int end = Math.min(start + MAX_SLOTS, allPrefixes.size());

		for (int i = start; i < end; i++) {
			var prefix = allPrefixes.get(i);

			// 장착 여부 확인: 장착 중인 게 있고, 현재 루프의 칭호와 같으면 true
			boolean isEquipped = (equippedPrefix != null && equippedPrefix.equals(prefix));

			ItemBuilder builder = ItemBuilder.of(plugin, Material.NAME_TAG)
					.setName(prefix.prefix())
					.setGlint(isEquipped); // 장착 중이면 반짝임

			if (isEquipped) {
				builder.setLore(ColorUtils.chat("&a▶ 현재 장착 중"), ColorUtils.chat("&7클릭하여 해제"));
			} else {
				builder.setLore(ColorUtils.chat("&7클릭하여 장착"));
			}

			inv.setItem(i - start, builder.build());
		}
	}

	// 페이지 변경 메서드
	public void nextPage(Player p) {
		this.page++;
		update(p);
	}

	public void prevPage(Player p) {
		if (this.page > 0) {
			this.page--;
			update(p);
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
				.setLore(ColorUtils.chat("&7이전 페이지로 이동합니다."))
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
