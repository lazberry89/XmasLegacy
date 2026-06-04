package xmasLegacy.HuntingZone;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.HuntingZone.CustomMobs.MobRepository;
import xmasLegacy.InfoLevel;
import xmasLegacy.XmasLegacy;

import java.util.List;

public class ZoneCommandManager implements CommandExecutor, TabCompleter {
	private final @NotNull MobRepository mr;
	private final @NotNull HuntingZoneManager hzm;
	private final @NotNull MobSpawnManager msm;
	private final @NotNull XmasLegacy plugin;

	public ZoneCommandManager() {
		this.mr = MobRepository.getInstance();
		this.hzm = HuntingZoneManager.getInstance();
		this.msm = MobSpawnManager.getInstance();
		this.plugin = XmasLegacy.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			plugin.infoMsg(InfoLevel.WARN, p, "You do not have permission to use this command.");
			return true;
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		return List.of();
	}
}
