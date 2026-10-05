package org.lazberry.xmaslegacy.shop.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.shop.showcase.ShowCaseManager;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record GoodsCommandHide(ShowCaseManager showCaseManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 1) return;
		var lst = player.getNearbyEntities(5, 5, 5);
		if (lst.isEmpty()) return;
		var entity = lst.getFirst();

		showCaseManager.removeInfoDisplay(entity);
	}
}
