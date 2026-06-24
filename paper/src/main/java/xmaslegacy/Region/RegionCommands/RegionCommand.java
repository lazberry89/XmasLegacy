package xmaslegacy.Region.RegionCommands;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Region.Gui.RegionSettingInterface;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Commands(command = "구역", aliases = {"region", "rg"})
public class RegionCommand implements CommandExecutor, TabCompleter {
	private final @NotNull Map<String, SubCommand> commands = new HashMap<>();

	public RegionCommand() {
		var setting = new RegionCommandSetting();
		var delete = new RegionCommandDelete();
		this.commands.put("입장", setting);
		this.commands.put("entry", setting);
		this.commands.put("interaction", setting);
		this.commands.put("상호작용", setting);
		this.commands.put("delete", delete);
		this.commands.put("삭제", delete);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		try {
			if (args.length == 0) {
				if (p.isOp()) p.getInventory().addItem(RegionManager.RegionTicket());
				else p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 사용법: /구역 <ID> <설정> <값>"));
				return true;
			}

			if (args.length == 1) {
				Region region = RegionManager.INSTANCE.getRegion(args[0]);
				if (region == null) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 아이디가 잘못되었습니다!"));
					return true;
				}
				p.openInventory(new RegionSettingInterface(region).getInventory());
				return true;
			}

			if (args[1].isBlank()) return true;

			SubCommand sub = this.commands.get(args[1].toLowerCase());
			if (sub == null) {
				InfoUtils.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 명령어입니다.");
				return true;
			}
			if (sub instanceof RegionCommandSetting && args.length < 3) {
				InfoUtils.infoMsg(InfoLevel.ERROR, p, "사용법: /구역 <ID> <설정> <값>");
				return true;
			}

			sub.execute(p, args);
			return true;
		} catch (Exception e) {
			log.error("Error occurred while executing command {}", label, e);
			InfoUtils.infoMsg(InfoLevel.ERROR, p, "명령어 사용 중 내부 예외가 발생했습니다.");
			return true;
		}
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
		if (!(commandSender instanceof Player p)) return List.of();
		var rm = RegionManager.INSTANCE;

		String lowerLabel = label.toLowerCase();
		boolean isEnglish = lowerLabel.equals("region") || lowerLabel.equals("rg");

		if (args.length == 1) {
			List<Region> targetList = p.isOp() ? rm.getRegions() : rm.getRegion(p);
			if (targetList.isEmpty()) return List.of();

			return targetList.stream()
					.map(Region::Id)
					.filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
					.toList();
		}

		if (args.length == 2) {
			Stream<String> subCommands = isEnglish
					? Stream.of("entry", "interaction", "delete")
					: Stream.of("입장", "상호작용", "삭제");

			return subCommands
					.filter(sub -> sub.toLowerCase().startsWith(args[1].toLowerCase()))
					.toList();
		}

		// 3. 허용/차단 값 추천
		if (args.length == 3) {
			String subCommand = args[1].toLowerCase();

			// 💡 유저가 혼용해서 쳤을 때를 대비해 한/영 서브커맨드 둘 다 방어벽에 넣어줍니다.
			if (subCommand.equals("입장") || subCommand.equals("entry") ||
					subCommand.equals("상호작용") || subCommand.equals("interaction")) {
				Stream<String> values = isEnglish
						? Stream.of("allow", "block")
						: Stream.of("허용", "차단");
				return values
						.filter(val -> val.toLowerCase().startsWith(args[2].toLowerCase()))
						.toList();
			}
		}
		return List.of();
	}
}