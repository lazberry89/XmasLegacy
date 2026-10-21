package org.lazberry.xmaslegacy.rank.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.rank.ProxyRankManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;

@Commands(command = "rank")
@Registry.Exclude(type = ServerType.LOBBY)
public class RankCommand implements CommandExecutor, TabCompleter {
	private final ProxyRankManager rankManager;

	@Inject
	public RankCommand(ProxyRankManager rankManager) {
		this.rankManager = rankManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		Component msg;
		if (args.length >= 1) {
			var offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
			if (!offlinePlayer.hasPlayedBefore()) {
				InfoUtils.error(p, "존재하지 않는 유저입니다.");
				return true;
			}
			var uuid = offlinePlayer.getUniqueId();
			msg = rankManager.getUserRankOverview(uuid);

			p.sendMessage(msg);
			return true;
		} else {
			msg = rankManager.getUserRankOverview(p.getUniqueId());
			p.sendMessage(msg);
			return true;
		}
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length == 1) {
			return null;
		}
		return List.of();
	}
}
