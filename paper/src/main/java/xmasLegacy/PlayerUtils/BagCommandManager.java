package xmasLegacy.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class BagCommandManager implements CommandExecutor, TabCompleter {
	private final BagManager BM;

	public BagCommandManager(BagManager BM) {
		this.BM = BM;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return false;
		if (args.length == 0) {
			p.openInventory(BM.getUserBags(p).getInventory());
			return true;
		} else if (args.length == 1) {
			if (!p.isOp()) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 권한이 없습니다!"));
				p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
				return true;
			} else {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
				if (target.hasPlayedBefore()) {
					p.openInventory(BM.getBag(target.getUniqueId()).getInventory());
					p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 플레이어 &6" + target.getName() + "&f의 가방을 조회중입니다."));
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					return true;
				}
			}

		} else {
			p.sendMessage(ColorUtils.chat("&cUsage: &7/bag"));
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (strings.length == 1) {
			if (!commandSender.isOp()) return List.of();
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
		}
		return List.of();
	}
}
