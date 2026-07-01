package org.lazberry.xmaslegacy.LogCommands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Annotation.Commands;
import org.lazberry.xmaslegacy.Utils.SubCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Commands(command = "log")
public class LogCommand implements CommandExecutor, TabCompleter {
	private final @NotNull Map<String, SubCommand> commandMap = new HashMap<>();

    public LogCommand() {
		this.commandMap.put("inquiry", new LogCommandInquiry());
		this.commandMap.put("inquiries", new LogCommandInquiries());
		this.commandMap.put("regions", new LogCommandRegions());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
        if (!(commandSender instanceof Player p)) return false;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 로그를 볼 수 있는 권한이 없습니다."));
            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            return true;
        }
		if (args.length == 0) return false;

		SubCommand sub = commandMap.get(args[0].toLowerCase());
		if (sub == null) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 명령어가 아닙니다."));
			p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
			return true;
		}
		sub.execute(p, args);
		return true;
    }

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			completions.add("inquiry");
			completions.add("inquiries");
			completions.add("regions");
		} else if (args.length == 2) {
			String sub = args[0].toLowerCase();
			if (sub.equals("inquiry") || sub.equals("regions")) {
				for (Player onlineP : Bukkit.getOnlinePlayers()) {
					completions.add(onlineP.getName());
				}
			}
		}

		String lastArg = args[args.length - 1];
		return completions.stream()
				.filter(s -> s.toLowerCase().startsWith(lastArg.toLowerCase()))
				.toList();
	}
}
