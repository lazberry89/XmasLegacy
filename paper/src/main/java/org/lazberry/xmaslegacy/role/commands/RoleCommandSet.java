package org.lazberry.xmaslegacy.role.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record RoleCommandSet(UserManager userManager) implements SubCommand {

	@Override //role set <name> <role>
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 3) {
			InfoUtils.error(player, "올바르지 않은 명령어에요!");
			return;
		}
		Player target = Bukkit.getPlayer(args[1]);
		if (target == null) {
			InfoUtils.error(player, "존재하지 않는 플레이어입니다.");
			return;
		}
		Role role = Role.parseRole(args[2]);
		if (role == null) {
			InfoUtils.error(player, "존재하지 않는 직업 명칭입니다.");
			return;
		}
		var user = userManager.getUser(target.getUniqueId());
		if (user == null) {
			InfoUtils.error(player, "유저 정보가 로드되지 않습니다.");
			return;
		}
		user.setRole(role);
		InfoUtils.info(player, "&6{}&f의 직업을 &6{}&f(으)로 변경했습니다.", target.getName(), role.getKor());
	}
}
