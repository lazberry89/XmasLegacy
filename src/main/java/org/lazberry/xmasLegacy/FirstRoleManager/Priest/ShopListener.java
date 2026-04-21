package org.lazberry.xmasLegacy.FirstRoleManager.Priest;

import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmasLegacy.Economy.EconomyManager;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.User.UserManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.List;

public class ShopListener implements Listener {
	private final PriestShop PSP;
	private final UserManager UM;
	private final EconomyManager ECM;
	private final XmasLegacy plugin;

	public ShopListener(PriestShop PSP, UserManager UM, EconomyManager ECM, XmasLegacy plugin) {
		this.PSP = PSP;
		this.UM = UM;
		this.ECM = ECM;
		this.plugin = plugin;
	}

	@EventHandler
	public void onTrade(PlayerTradeEvent e) {
		Player viewer = e.getPlayer();
		Player owner = PSP.getOwner(e.getMerchant());
		if (owner == null) {
			viewer.sendMessage(ColorUtils.chat(Prefix.RED + " 주인장이 문을 닫았네요!"));
			e.setCancelled(true);
			return;
		}
		List<ItemStack> ingredients = e.getTrade().getIngredients();
		ItemStack money = ingredients.getFirst();
		NamespacedKey key = plugin.getNamespacedKey("money");

		Integer mc = money.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
		if (mc == null) return;
		int count = mc * ingredients.size();

		if (count <= 0) return;
		ECM.deposit(owner, count);
		owner.playSound(owner, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
	}
}
