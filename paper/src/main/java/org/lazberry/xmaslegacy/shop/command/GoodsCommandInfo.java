package org.lazberry.xmaslegacy.shop.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.shop.showcase.ShowCaseManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record GoodsCommandInfo(ShowCaseManager showCaseManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 1) {
			InfoUtils.error(player, "올바르지 않는 명령어입니다.");
			return;
		}
		var block = player.getTargetBlockExact(10);
		if (block == null) {
			InfoUtils.error(player, "블록을 보고 사용해주세요.");
			return;
		}
		var loc = block.getLocation();
		showCaseManager.spawnInfoTextDisplay(player, loc);
	}
}
