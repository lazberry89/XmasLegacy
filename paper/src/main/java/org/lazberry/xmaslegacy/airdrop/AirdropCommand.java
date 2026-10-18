package org.lazberry.xmaslegacy.airdrop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Commands(command = "supply")
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropCommand implements CommandExecutor {
	private final AirdropManager airdropManager;

	@Inject
	public AirdropCommand(AirdropManager airdropManager) {
		this.airdropManager = airdropManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		var loc = p.getLocation();
		airdropManager.spawnAirDrop(loc);
		return true;
	}
}
