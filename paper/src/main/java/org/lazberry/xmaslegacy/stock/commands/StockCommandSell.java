package org.lazberry.xmaslegacy.stock.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.stock.StockManager;

public record StockCommandSell(StockManager sm) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("판매")) {
				var item = player.getInventory().getItemInMainHand();
				if (item.getType().isAir()) {
					InfoUtils.error(player, "판매할 &6주권 아이템&f을 손에 들어주세요!");
					return;
				}
				if (sm.isStockCertificate(item)) {
					switch (sm.sellStock(player, item)) {
						case SUCCESS -> InfoUtils.info(player, "주권 판매가 완료되었습니다!");
						case TIMEOUT -> InfoUtils.info(player, "아직 시장이 개장되지 않았습니다.");
						default -> {
							InfoUtils.error(player, "판매에 실패하였습니다!");
							InfoUtils.warn(player, "유저 정보가 &c소실&f되었거나, 주식이 존재하지 않을 수 있습니다. &7(상장폐지)");
						}
					}
				} else InfoUtils.error(player, "주식 확인증이 아닙니다. 확인증을 손에 들어주세요!");
			} else InfoUtils.error(player, "올바른 명령어 사용법이 아닙니다.");
		} else {
			InfoUtils.error(player, "올바른 명령어 사용법이 아닙니다.");
			InfoUtils.warn(player, "사용법: /주식 판매");
		}
	}
}
