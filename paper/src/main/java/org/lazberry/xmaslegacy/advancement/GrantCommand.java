package org.lazberry.xmaslegacy.advancement;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "grant")
@Registry.Exclude(type = ServerType.LOBBY)
public class GrantCommand implements CommandExecutor {
	private final XmasLegacyAdvancements advancements;

	@Inject
	public GrantCommand(XmasLegacyAdvancements advancements) {
		this.advancements = advancements;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!sender.isOp()) return true;

		if (args.length == 1) {
			try {
				// 💡 플러그인 대신 탭 네임스페이스("xmaslegacy_global")를 사용하거나
				// 사용자가 입력한 전체 문자열을 그대로 AdvancementKey로 변환합니다.
				com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey key;

				if (args[0].contains(":")) {
					key = com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey.fromString(args[0]);
				} else {
					// 기본 탭의 키인 경우 탭 네임스페이스 적용
					key = new com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey("xmaslegacy_global", args[0]);
				}

				var give = advancements.getApi().getAdvancement(key);
				if (give != null) {
					advancements.grantGlobalAdvancement(p, give);
					p.sendMessage("§a[성공] 과제 지급 완료: " + key);
				} else {
					p.sendMessage("§c[실패] 해당 키를 가진 과제를 찾을 수 없습니다: " + key);
				}
			} catch (Exception e) {
				p.sendMessage("§c[에러] 예외 발생: " + e.getMessage());
			}
		}
		return true;
	}
}
