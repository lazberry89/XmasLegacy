package xmasLegacy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.settings.Prefix;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class OperatorCurrency implements CommandExecutor, TabCompleter {
	private final EconomyManager ecm;

	public OperatorCurrency(EconomyManager ecm) {
		this.ecm = ecm;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		if (args.length == 3) { //currency add Lazberry89 100
			Player target = Bukkit.getPlayerExact(args[1]);
			if (target == null) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 플레이어가 아닙니다!"));
				return true;
			}
			int amount;
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 올바른 숫자를 입력해주세요!"));
				return true;
			}

			switch (args[0].toLowerCase()) {
				case "add" -> {
					if (amount < 0) {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 음수는 입력할 수 없습니다!"));
						return true;
					}
					if (ecm.deposit(target.getUniqueId(), amount)) {
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 해당 유저의 코인 -> " + amount));
					} else {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 입금에 실패하였습니다."));
					}
				}
				case "remove" -> {
					if (amount < 0) {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 음수는 입력할 수 없습니다!"));
						return true;
					}
					if (ecm.withdraw(target.getUniqueId(), amount)) {
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 해당 유저의 코인 -> " + amount));
					} else {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 출금에 실패하였습니다."));
					}
				}
				case "set" -> {
					if (amount < 0) {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 음수는 입력할 수 없습니다!"));
						return true;
					}
					if (ecm.setBalance(target.getUniqueId(), amount)) {
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 해당 유저의 코인 -> " + amount));
					} else {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 자금설정에 실패하였습니다."));
					}
				}
				case "check" -> p.sendMessage(ColorUtils.chat(Prefix.YELLOW + target.getName() + "의 코인: " + ecm.checkBalance(target.getUniqueId())));
				default -> p.sendMessage(ColorUtils.chat(Prefix.RED + " 잘못된 명령어입니다!"));
			}
		} else if (args.length == 4) {
			Player target = Bukkit.getPlayerExact(args[1]);
			Player toTarget = Bukkit.getPlayerExact(args[2]);
			if (target == null || toTarget == null) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 플레이어가 아닙니다!"));
				return true;
			}
			if (args[0].equalsIgnoreCase("transfer")) {
				int amount;
				try {
					amount = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					p.sendMessage(ColorUtils.chat(Prefix.RED + " 올바른 숫자를 입력해주세요!"));
					return true;
				}
				if (amount < 0) {
					p.sendMessage(ColorUtils.chat(Prefix.RED + " 음수는 입력할 수 없습니다!"));
					return true;
				}
				if (ecm.transferMoney(target.getUniqueId(), toTarget.getUniqueId(), amount)) {
					p.sendMessage(ColorUtils.chat(Prefix.YELLOW + target.getName() + "의 코인 -> " + amount + " -> " + toTarget.getName()));
				} else {
					p.sendMessage(ColorUtils.chat(Prefix.RED + " 송금에 실패하였습니다."));
				}
			} else {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 잘못된 명령어입니다!"));
			}
		} else {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 잘못된 명령어입니다!"));
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			String[] subCommands = {"add", "remove", "set", "check", "transfer"};
			for (String sub : subCommands) {
				if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
			}
			return completions;
		}

		if (args.length == 2) {
			return null;
		}

		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("transfer")) {
				return null;
			} else {
				completions.add("<금액>");
				return completions;
			}
		}

		if (args.length == 4 && args[0].equalsIgnoreCase("transfer")) {
			completions.add("<금액>");
			return completions;
		}

		return completions;
	}
}
