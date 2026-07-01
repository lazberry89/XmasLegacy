package org.lazberry.xmaslegacy.PartyCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Commands;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Commands(command = "파티", aliases = {"party", "pt"})
public class PartyCommand implements CommandExecutor, TabCompleter {
	private final @NotNull Map<String, SubCommand> commands = new HashMap<>();

    public PartyCommand() {
		this.commands.put("invite", new PartyCommandInvite());
		this.commands.put("초대", new PartyCommandInvite());
		this.commands.put("join", new PartyCommandJoin());
		this.commands.put("참가", new PartyCommandJoin());
		this.commands.put("member", new PartyCommandMember());
		this.commands.put("멤버", new PartyCommandMember());
		this.commands.put("expel", new PartyCommandExpel());
		this.commands.put("추방", new PartyCommandExpel());
		this.commands.put("create", new PartyCommandCreate());
		this.commands.put("생성", new PartyCommandCreate());
		this.commands.put("leave", new PartyCommandLeave());
		this.commands.put("나가기", new PartyCommandLeave());
    }

    //파티 초대 <이름>
    //파티 참가 <이름>
    //파티 멤버 <이름>
    //파티 추방 <이름>
    //파티 생성
    //파티 나가기
    //파티 멤버
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
		if (args.length == 0) return true;

		SubCommand sub = commands.get(args[0]);
		if (sub == null) {
			InfoUtils.error(p, "유효하지 않는 명령어입니다.");
			return true;
		}
		sub.execute(p, args);

        return true;
    }

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
		if (args.length == 1) {
			String lowerLabel = label.toLowerCase();

			if (lowerLabel.equals("party") || lowerLabel.equals("pt"))
				return Stream.of("help", "create", "member", "leave", "invite", "join", "expel")
						.filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
						.collect(Collectors.toList());

			return Stream.of("도움", "생성", "멤버", "나가기", "초대", "참가", "추방")
					.filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
					.collect(Collectors.toList());
		}

		if (args.length == 2) {
			String subCommand = args[0].toLowerCase();
			if (subCommand.equals("초대") || subCommand.equals("invite") ||
					subCommand.equals("참가") || subCommand.equals("join") ||
					subCommand.equals("추방") || subCommand.equals("expel") ||
					subCommand.equals("멤버") || subCommand.equals("member"))

				return Bukkit.getOnlinePlayers().stream()
						.map(Player::getName)
						.filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
						.collect(Collectors.toList());

		}
		return Collections.emptyList();
	}
}
