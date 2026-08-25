package org.lazberry.xmaslegacy.stock.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;

public record StockCommandList(StockManager sm) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length >= 1 &&
				(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("목록"))) {
			var stocks = sm.getStocks();
			if (stocks.isEmpty()) {
				InfoUtils.warn(player, "등록된 주식이 현재 없습니다. 조금만 기다려주세요!");
				return;
			}
			Component message = ColorUtils.chat("&6&l[ &f&l주식 시장 리스트 &6&l]")
					.appendNewline()
					.append(ColorUtils.chat("&7&m--------------------------------"));

			for (Stock stock : stocks) {
				message = message.appendNewline()
						.append(ColorUtils.chat(String.format(" &f%s &7| &e%,.0f원", stock.getFormatStringMessage(), stock.getCurrentPrice())));
			}
			message = message.appendNewline()
					.append(ColorUtils.chat("&7&m--------------------------------"));
			player.sendMessage(message);
		} else {
			InfoUtils.error(player, "올바른 명령어 사용법이 아닙니다.");
			InfoUtils.warn(player, "사용법: /주식 목록");
		}
	}
}
