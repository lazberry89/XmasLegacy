package org.lazberry.xmaslegacy.blueprint.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record BlueprintCommandShop(BluePrintManager blueprintManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		blueprintManager.openShop(player);
	}
}
