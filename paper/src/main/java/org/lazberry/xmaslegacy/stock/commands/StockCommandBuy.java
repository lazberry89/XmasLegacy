package org.lazberry.xmaslegacy.stock.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.stock.StockManager;

public record StockCommandBuy(StockManager sm) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length >= 3) {
			var stockName = args[1];
			int amount;
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				InfoUtils.error(player, "숫자를 입력해주세요!");
				return;
			}
			if (amount <= 0) {
				InfoUtils.error(player, "구매 수량은 양수여야합니다!");
				return;
			}
			var optionalStock = sm.getStock(stockName);
			if (optionalStock.isEmpty()) {
				InfoUtils.error(player, "존재하지 않는 주식입니다! 이름을 확인해보세요: &7{}", stockName);
				return;
			}
			var stock = optionalStock.get();
			if (sm.buyStock(player, stock, amount)) InfoUtils.info(player, "구매에 성공했습니다! &6({}, {}개)", stockName, amount);
			else {
				InfoUtils.error(player, "주권 구매에 실패했습니다. &7({}, {}개)", stockName, amount);
				InfoUtils.warn(player, "&7금액이 부족한지 확인해보세요! (현재주가: {}원)", stock.getCurrentPrice());
			}
		} else {
			InfoUtils.error(player, "올바른 명령어 사용법이 아닙니다!");
			InfoUtils.warn(player, "사용법: /주식 구매 <주식이름> <수량>");
		}
	}
}
