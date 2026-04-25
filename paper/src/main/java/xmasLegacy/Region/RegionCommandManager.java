package xmasLegacy.Region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class RegionCommandManager implements CommandExecutor, TabCompleter {
	private final RegionManager RM;

	public RegionCommandManager(RegionManager RM) {
		this.RM = RM;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 0) {
			p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 사용법: /구역 <ID> <설정> <값>"));
			return true;
		} else if (args.length == 3) {
			Region region = RM.getRegion(args[0]);
			if (region == null) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 아이디가 잘못되었습니다!"));
				return true;
			}
			if (!p.isOp() || !region.getOwner().equals(p.getUniqueId())) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 권한이 없습니다!"));
				return true;
			}
			switch (args[1]) {
				case "입장" -> {
					if (args[2].equals("허용")) {
						region.setAllowPublicEntry(true);
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 입장을 허용했습니다."));
					} else {
						region.setAllowPublicEntry(false);
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 입장을 차단했습니다."));
					}
				}
				case "상호작용" -> {
					if (args[2].equals("허용")) {
						region.setAllowPublicInteraction(true);
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 상호작용을 허용했습니다."));
					} else {
						region.setAllowPublicInteraction(false);
						p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 상호작용을 차단했습니다."));
					}
				}
			}
		} else if (args.length == 2) {
			Region region = RM.getRegion(args[0]);
			if (region == null) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 아이디가 잘못되었습니다!"));
				return true;
			}
			if (!p.isOp() || !region.getOwner().equals(p.getUniqueId())) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 권한이 없습니다!"));
				return true;
			}
			if (args[1].equals("삭제")) {
				RM.removeRegion(region);
				p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 구역을 삭제했습니다. &6ID: " + region.getId()));
				return true;
			}
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(commandSender instanceof Player p)) return List.of();

		if (args.length == 1) {
			// 본인 구역 ID들 추천 (OP는 전체)
			List<Region> targetList = p.isOp() ? RM.getRegions() : RM.getRegion(p);
			if (targetList == null) return List.of();
			return targetList.stream().map(Region::getId).toList();
		}

		if (args.length == 2) {
			return List.of("입장", "상호작용", "삭제");
		}

		if (args.length == 3) {
			if (args[1].equals("입장") || args[1].equals("상호작용")) {
				return List.of("허용", "차단");
			}
		}

		return List.of();
	}
}