package org.lazberry.xmaslegacy.advancement;

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.listener.EnchantBrokenPreventedEvent;
import org.lazberry.xmaslegacy.enchant.listener.EnchantDowngradePreventEvent;
import org.lazberry.xmaslegacy.enchant.listener.PlayerEnchantEvent;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.region.event.RegionGenerateEvent;
import org.lazberry.xmaslegacy.utils.KeyUtils;

/** Advancements that an existing event already announces. The rest call {@link Advancements#grant} in place. */
@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class AdvancementGrantListener implements Listener {
	private static final NamespacedKey BOOSTERS_USED = KeyUtils.get("boosters_used");
	private static final int FIVE_PERCENT = 1, TEN_PERCENT = 2;

	private final XmasLegacyAdvancements advancements;

	@Inject
	public AdvancementGrantListener(XmasLegacyAdvancements advancements) {
		this.advancements = advancements;
	}

	@EventHandler
	public void grantEarnedWhileLoading(PlayerLoadingCompletedEvent e) {
		advancements.grantPending(e.getPlayer());
	}

	@EventHandler
	public void forgetOnQuit(PlayerQuitEvent e) {
		advancements.forgetPending(e.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void buyRegion(RegionGenerateEvent e) {
		Advancements.grant(e.getPlayer(), "buy_region");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void plantTorchflower(BlockPlaceEvent e) {
		if (e.getBlockPlaced().getType() == Material.TORCHFLOWER_CROP)
			Advancements.grant(e.getPlayer(), "plant_torch_flower");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void craftBread(CraftItemEvent e) {
		if (e.getWhoClicked() instanceof Player p && isSunflowerBread(e.getCurrentItem()))
			Advancements.grant(p, "sunflower_bread_create");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void eatBread(PlayerItemConsumeEvent e) {
		if (isSunflowerBread(e.getItem())) Advancements.grant(e.getPlayer(), "consume_sunflower_bread");
	}

	private static boolean isSunflowerBread(ItemStack item) {
		return KeyUtils.hasKey(item, KeyUtils.get("farmer"), PersistentDataType.STRING, "sunflower_bread");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void enchant(PlayerEnchantEvent e) {
		Player p = e.getPlayer();
		Advancements.grant(p, "first_enchant");

		// EnchantItemListener (HIGH) already consumed a booster and set its bonus.
		if (e.getBonusChance() <= 0) return;
		Advancements.grant(p, "booster_use");

		var data = p.getPersistentDataContainer();
		int used = data.getOrDefault(BOOSTERS_USED, PersistentDataType.INTEGER, 0)
				| (e.getBonusChance() >= 10.0 ? TEN_PERCENT : FIVE_PERCENT);
		data.set(BOOSTERS_USED, PersistentDataType.INTEGER, used);
		if (used == (FIVE_PERCENT | TEN_PERCENT)) Advancements.grant(p, "booster_all_use");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void preventDowngrade(EnchantDowngradePreventEvent e) {
		Advancements.grant(e.getPlayer(), "downgrade_protect");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void preventBreak(EnchantBrokenPreventedEvent e) {
		Advancements.grant(e.getPlayer(), "break_protect");
	}
}
