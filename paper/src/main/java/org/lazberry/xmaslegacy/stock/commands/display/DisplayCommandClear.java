package org.lazberry.xmaslegacy.stock.commands.display;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.stock.display.StockDisplayConfig;
import org.lazberry.xmaslegacy.stock.display.StockDisplayManager;

public record DisplayCommandClear(StockDisplayManager sdm, StockDisplayConfig sdc) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 1) return;

		sdm.clear();
		InfoUtils.warn(player, "모든 디스플레이를 삭제했습니다.");
		sdc.saveAll().whenComplete((v, e) -> {
			if (e == null) InfoUtils.info(player, "성공적으로 저장했습니다.");
			else InfoUtils.error(player, "저장하지 못했습니다.");
		});
	}
}
