package org.lazberry.xmaslegacy.Icing;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "icing")
@Registry.Exclude(type = ServerType.LOBBY)
public class IcingCommand implements CommandExecutor {
	private final @NotNull IcingSystem system;

	@Inject
	public IcingCommand(@NotNull IcingSystem system) {
		this.system = system;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull ...args) {
		var plugin = XmasLegacy.getInstance();
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			InfoUtils.error(p, "관리자용 명령어에요!");
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("task")) {
				if (system.isTaskRunning()) {
					system.stopTask();
					InfoUtils.info(p, "빙결 시스템을 정지하였습니다.");
				} else {
					system.startTask(plugin);
					InfoUtils.info(p, "빙결 시스템을 시작하였습니다.");
				}
			}
		}
		return true;
	}
}
