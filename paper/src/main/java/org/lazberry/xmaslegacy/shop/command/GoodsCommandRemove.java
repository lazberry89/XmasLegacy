package org.lazberry.xmaslegacy.shop.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.shop.showcase.ShowCaseManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record GoodsCommandRemove(ShowCaseManager showCaseManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 2) {
			InfoUtils.error(player, "올바르지 않는 명령어입니다.");
			return;
		}
		String name = args[1];
		if (showCaseManager.removeGoods(name)) InfoUtils.info(player, "성공적으로 상품을 삭제했습니다.");
		else InfoUtils.error(player, "존재하지 않는 상품입니다.");
	}
}
