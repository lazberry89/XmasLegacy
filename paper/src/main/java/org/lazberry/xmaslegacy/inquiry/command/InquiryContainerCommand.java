package org.lazberry.xmaslegacy.inquiry.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.inquiry.InquiryManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;

@Commands(command = "문의함")
@Registry.Include(type = ServerType.GLOBAL)
public class InquiryContainerCommand implements CommandExecutor, TabCompleter {
	private final InquiryManager inquiryManager;

	@Inject
	public InquiryContainerCommand(InquiryManager inquiryManager) {
		this.inquiryManager = inquiryManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;

		var uuid = p.getUniqueId();
		if (!p.isOp()) {
			p.sendMessage(inquiryManager.getInquiryHistoryText(uuid));
			inquiryManager.clearAllUnreadInquiries(uuid);
			return true;
		} else {
			if (args.length == 0) {
				inquiryManager.getInquiries().forEach(inquiry ->
					p.sendMessage(inquiryManager.inquiryStateText(inquiry)));
			} else {
				String name = args[0];
				OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(name);
				if (target != null) {
					p.sendMessage(inquiryManager.getInquiryHistoryText(target.getUniqueId()));
					return true;
				} else InfoUtils.error(p, "유효하지 않은 플레이어입니다.");
			}
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length == 1 && sender.isOp()) return null;
		return List.of();
	}
}
