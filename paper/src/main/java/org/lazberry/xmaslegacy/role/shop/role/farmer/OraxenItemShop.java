package org.lazberry.xmaslegacy.role.shop.role.farmer;

import org.lazberry.xmaslegacy.utils.ReadOnlyGui;
import org.lazberry.xmaslegacy.advancement.Advancements;
import io.th0rgal.oraxen.api.OraxenItems;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.List;
import java.util.function.Supplier;

/**
 * A farmer shop page that buys or sells Oraxen items (CustomCrops crops, seeds and tools) by Oraxen id.
 * Slot 0 is the back button; entries fill from slot 9. The inventory is built on first open,
 * because Oraxen registers its items after this plugin's components are created.
 */
@Slf4j
public class OraxenItemShop implements ReadOnlyGui {
	public enum Mode { BUY, SELL }

	/** {@code factory} builds a non-Oraxen item (sold only); without it the item is the Oraxen item {@code id}. */
	public record Entry(String id, int price, @Nullable Supplier<ItemStack> factory) {
		public Entry(String id, int price) {
			this(id, price, null);
		}
	}

	private static final int FIRST_SLOT = 9;

	private final String title;
	private final Mode mode;
	private final List<Entry> entries;
	private final int size;
	private Entry[] bySlot;
	private Inventory inv;

	public OraxenItemShop(String title, Mode mode, List<Entry> entries) {
		this.title = title;
		this.mode = mode;
		this.entries = entries;
		this.size = Math.min(54, (FIRST_SLOT + entries.size() + 8) / 9 * 9);
	}

	@Override
	public @NotNull Inventory getInventory() {
		if (inv == null) build();
		return inv;
	}

	public @Nullable Entry entry(int slot) {
		if (bySlot == null || slot < 0 || slot >= bySlot.length) return null;
		return bySlot[slot];
	}

	private void build() {
		Inventory built = Bukkit.createInventory(this, size, ColorUtils.chat(title));
		Entry[] slots = new Entry[size];
		var bg = InventoryHelper.background();
		for (int i = 0; i < size; i++) built.setItem(i, bg);
		built.setItem(0, InventoryHelper.back());

		int slot = FIRST_SLOT;
		for (Entry e : entries) {
			if (slot >= size) break;
			ItemStack icon = item(e);
			if (icon == null) {
				log.warn("Farmer shop '{}': Oraxen item '{}' does not exist, skipped.", title, e.id());
				continue;
			}
			String action = mode == Mode.BUY ? "&7클릭하여 구매하세요." : "&7클릭하여 1개, 쉬프트+클릭하여 전부 판매하세요.";
			built.setItem(slot, ItemBuilder.of(XmasLegacy.getInstance(), icon)
					.addLore(ColorUtils.chat(""), ColorUtils.chat(action), ColorUtils.chat(String.format("&7가격: &6%,d$", e.price())))
					.build());
			slots[slot++] = e;
		}
		this.bySlot = slots;
		this.inv = built;
	}

	public void buy(Player p, UserManager userManager, Entry e) {
		ItemStack item = item(e);
		if (item == null) return;
		if (userManager.withdraw(p.getUniqueId(), e.price())) {
			InventoryHelper.giveItemOrDrop(p, item);
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			InfoUtils.info(p, "구매하였습니다.");
			User user = userManager.getUser(p.getUniqueId());
			if (user != null && user.hasRole()) Advancements.grant(p, "use_role_shop");
		} else {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			InfoUtils.error(p, "잔액이 부족합니다.");
		}
	}

	public void sell(Player p, User user, Entry e, boolean all) {
		ItemStack[] storage = p.getInventory().getStorageContents();
		int owned = 0;
		for (ItemStack it : storage) if (matches(it, e.id())) owned += it.getAmount();

		int amount = all ? owned : 1;
		if (owned <= 0 || amount <= 0) {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}

		int remain = amount;
		for (int i = 0; i < storage.length && remain > 0; i++) {
			if (!matches(storage[i], e.id())) continue;
			int take = Math.min(remain, storage[i].getAmount());
			storage[i].setAmount(storage[i].getAmount() - take);
			if (storage[i].getAmount() <= 0) storage[i] = null;
			remain -= take;
		}
		p.getInventory().setStorageContents(storage);
		user.addDollars((long) e.price() * amount);
		Advancements.grant(p, "earn_money");
		if (user.hasRole()) Advancements.grant(p, "use_role_shop");
		p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
	}

	public Mode mode() {
		return mode;
	}

	private static boolean matches(@Nullable ItemStack item, String id) {
		return item != null && !item.getType().isAir() && id.equals(OraxenItems.getIdByItem(item));
	}

	private static @Nullable ItemStack item(Entry e) {
		if (e.factory() != null) return e.factory().get();
		var builder = OraxenItems.getItemById(e.id());
		return builder == null ? null : builder.build();
	}
}
