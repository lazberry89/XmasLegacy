package org.lazberry.xmaslegacy.sin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Commands(command = "nightmare")
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class NightmareCommand implements CommandExecutor {
	private final NightmareManager nightmareManager;

	@Inject
	public NightmareCommand(NightmareManager nightmareManager) {
		this.nightmareManager = nightmareManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (args.length == 0) {
			nightmareManager.blackPlayer(p);
		} else nightmareManager.resetPlayer(p);
		return true;
	}
}
