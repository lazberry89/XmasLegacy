package org.lazberry.xmaslegacy.api.donate.action;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.api.ProductType;
import org.lazberry.xmaslegacy.currency.CurrencyManager;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Slf4j
public class StarterPackAction extends ProvideAction {
	private final XmasLegacy plugin;

    @Getter
    private final ProductType productType;

    public StarterPackAction(XmasLegacy plugin) {
	    this.plugin = plugin;
	    this.productType = ProductType.STARTER_PACK;
    }

	@Override
    public void grantProduct(Player player) {
		if (player != null && player.isOnline()) {
			var bread = AgeableCrops.SunFlowerBread();
			bread.setAmount(64);

			var money = CurrencyManager.currency(plugin);
			money.setAmount(99);

			InventoryHelper.giveItemOrKeep(player, bread, money, money, money);
		}
    }
}
