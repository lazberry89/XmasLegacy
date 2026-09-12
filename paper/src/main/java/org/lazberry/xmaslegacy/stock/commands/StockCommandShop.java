package org.lazberry.xmaslegacy.stock.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.stock.StockManager;
import org.lazberry.xmaslegacy.stock.shop.StockShopManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record StockCommandShop(StockShopManager ssm, StockManager sm) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length >= 1) {
			boolean open = sm.isOpen();

			if (open) {
				ssm.openShop(player);
				InfoUtils.info(player, "상점을 열었습니다.");
			} else InfoUtils.error(player, "주식시장이 닫혀있습니다. 나중에 다시 방문해주세요!");
		}
	}
}
