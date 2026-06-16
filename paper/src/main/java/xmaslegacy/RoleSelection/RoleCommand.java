package xmaslegacy.RoleSelection;

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
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
@Commands(command = "role")
public class RoleCommand implements CommandExecutor, TabCompleter {
	private final XmasLegacy plugin;
	private final UserManager um;

	public RoleCommand() {
		this.plugin = XmasLegacy.getInstance();
		this.um = UserManager.INSTANCE;
	}

	@Override // /role Player USER
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 관리자용 명령어에요!"));
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return true;
		}
		if (args.length == 2) {
			Player target = Bukkit.getPlayerExact(args[0]);
			if (target == null) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 플레이어를 찾을 수 없습니다."));
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
				return true;
			}
			User user = um.getUser(target.getUniqueId());
			if (user == null) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 플레이어를 찾을 수 없습니다."));
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
				return true;
			}
			try {
				user.setRole(BasicRoles.valueOf(args[1]));
			} catch (IllegalArgumentException e) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 역할을 찾을 수 없습니다."));
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
				return true;
			}
			p.sendMessage(ColorUtils.chat(Alert.GREEN + " " + target.getName() + "님의 역할이 " + BasicRoles.valueOf(args[1]).getKor() + "(으)로 설정되었습니다."));
			p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
			return true;
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		List<String> result = new ArrayList<>();
		if (strings.length == 1) {
			return null;
		} else if (strings.length == 2) {
			result.add("USER");
			result.add("WARRIOR");
			result.add("ROGUE");
			result.add("MAGE");
			result.add("KNIGHT");
			result.add("CRAFTER");
			result.add("ARCHER");
			result.add("PRIEST");
			result.add("MINER");
			result.add("MERCHANT");
			result.add("GATHERER");
			result.add("FARMER");
		}
		return result;
	}
}
