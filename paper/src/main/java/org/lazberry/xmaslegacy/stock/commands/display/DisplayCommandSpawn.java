package org.lazberry.xmaslegacy.stock.commands.display;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;
import org.lazberry.xmaslegacy.stock.display.StockDisplayConfig;
import org.lazberry.xmaslegacy.stock.display.StockDisplayManager;

import java.util.ArrayList;
import java.util.List;

public record DisplayCommandSpawn(StockManager sm, StockDisplayManager sdm, StockDisplayConfig sdc) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 2) return;
		List<Stock> result = new ArrayList<>();

		for (int i = 1; i < args.length; i++) {
			String stockName = args[i];
			var optional = sm.getStock(stockName);

			if (optional.isEmpty()) {
				InfoUtils.error(player, "존재하지 않는 주식입니다: %s", stockName);
				return;
			}
			result.add(optional.get());
		}

		if (sdm.add(player.getLocation(), result.toArray(Stock[]::new))) {
			InfoUtils.info(player, "성공적으로 디스플레이를 생성했습니다.");
			sdc.saveAll().whenComplete((v, e) -> {
				if (e == null) InfoUtils.info(player, "성공적으로 저장했습니다.");
				else InfoUtils.error(player, "저장하지 못했습니다.");
			});
		}
		else InfoUtils.error(player, "소환에 실패했습니다.");
	}
}
