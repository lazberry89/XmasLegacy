package org.lazberry.xmaslegacy.inquiry.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.inquiry.ServerInquiryManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;

@Commands(command = "문의")
@Registry.Include(type = ServerType.GLOBAL)
public class InquiryCommand implements CommandExecutor, TabCompleter {
	private final ServerInquiryManager serverInquiryManager;

	@Inject
	public InquiryCommand(ServerInquiryManager serverInquiryManager) {
		this.serverInquiryManager = serverInquiryManager;
	}

	@Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
		if (args.length == 0) {
			InfoUtils.error(p, "문의할 내용을 입력해주세요!");
			return true;
		}
		String contents = String.join(" ", args);
		serverInquiryManager.sendInquiry(p, contents);
		return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
