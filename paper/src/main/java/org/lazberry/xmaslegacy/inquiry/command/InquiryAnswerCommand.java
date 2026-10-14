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

import java.util.Arrays;
import java.util.List;

@Commands(command = "문의답변")
@Registry.Include(type = ServerType.GLOBAL)
public class InquiryAnswerCommand implements CommandExecutor, TabCompleter {
	private final ServerInquiryManager serverInquiryManager;

	@Inject
	public InquiryAnswerCommand(ServerInquiryManager serverInquiryManager) {
		this.serverInquiryManager = serverInquiryManager;
	}

	@Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) {
			InfoUtils.error(p, "사용할 권한이 없습니다.");
			return true;
		}
		if (args.length >= 2) {
			long id;
			try {
				id = Long.parseLong(args[0]);
			} catch (NumberFormatException e) {
				InfoUtils.error(p, "숫자를 입력해주세요.");
				return true;
			}
			String contents = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			serverInquiryManager.answerInquiry(p, id, contents);
		} else InfoUtils.error(p, "유효하지 않은 명령어입니다.");
		return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
