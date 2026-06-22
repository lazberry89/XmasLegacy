package xmaslegacy.PlayerUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.XmasLegacy;

@Commands(command = "clearpdc")
public class PdcClearCommand implements CommandExecutor {


	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		var plugin = XmasLegacy.getInstance();
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			plugin.infoMsg(InfoLevel.ERROR, p, "관리자용 명령어에요!");
			return true;
		}
		if (args.length == 1) {
			var key = plugin.getNamespacedKey(args[0]);
			if (p.getPersistentDataContainer().has(key)) {
				p.getPersistentDataContainer().remove(key);
				plugin.infoMsg(InfoLevel.INFO, p, "제거하였습니다.");
			} else {
				plugin.infoMsg(InfoLevel.ERROR, p, "키를 소유하고 있지 않습니다.");
			}
		}
		return true;
	}
}
