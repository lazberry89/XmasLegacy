package xmaslegacy.HuntingZone;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.HuntingZone.CustomMobs.MobRepository;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused, FieldCanBeLocal")
@Commands(command = "zone")
public class ZoneCommandManager implements CommandExecutor, TabCompleter {
	private final @NotNull MobRepository mr;
	private final @NotNull HuntingZoneManager hzm;
	private final @NotNull MobSpawnManager msm;
	private final @NotNull XmasLegacy plugin;

	public ZoneCommandManager() {
		this.mr = MobRepository.INSTANCE;
		this.hzm = HuntingZoneManager.INSTANCE;
		this.msm = MobSpawnManager.INSTANCE;
		this.plugin = XmasLegacy.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			plugin.infoMsg(InfoLevel.WARN, p, "관리자용 명령어에요!");
			return true;
		}
		Chunk chunk = p.getChunk();
		if (args.length == 2) {
			ZoneType type;
			try {
				type = ZoneType.valueOf(args[1]);
			} catch (IllegalArgumentException e) {
				plugin.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 타입입니다. \"" + args[1] + "\"");
				return true;
			}
			HuntingZone zone = hzm.getZones(type);
			if (zone == null) {
				plugin.infoMsg(InfoLevel.ERROR, p, "해당 사냥터가 설정되지 않았거나 적절하지 않습니다.");
				return true;
			}
			switch (args[0].toLowerCase()) {
				case "get" -> {
					Chunk[] chunks = zone.zones();
					if (chunks.length == 0) {
						plugin.infoMsg(InfoLevel.WARN, p, "해당 사냥터의 구역이 아직 설정되지 않았습니다.");
						return true;
					}
					plugin.infoMsg(InfoLevel.WARN, p, "로드된 청크에 한하여 출력됩니다.");
					Arrays.stream(chunks).forEach(c -> p.sendMessage(ColorUtils.chat(String.format("&6x&f : %d, &6z&f : %d", c.getX(), c.getZ()))));
				}
				case "spawn" -> {
					if (zone.isEnabled()) {
						plugin.infoMsg(InfoLevel.WARN, p, "이미 활성화되어 있습니다.");
					} else {
						plugin.infoMsg(InfoLevel.INFO, p, "활성화 하였습니다.");
						zone.enable();
					}
				}
				case "despawn" -> {
					if (zone.isEnabled()) {
						plugin.infoMsg(InfoLevel.INFO, p, "비활성화 하였습니다.");
						zone.disable();
					} else {
						plugin.infoMsg(InfoLevel.WARN, p, "이미 비활성화 상태입니다.");
					}
				}
				case "alive" -> plugin.infoMsg(InfoLevel.WARN, p, String.format("현재 사냥터 몹 수(최대마릿수 초과시 검사필요): %d/%d", zone.getAliveMobCount(), zone.getMaxSpawn()));
				case "expand" -> {
					if (chunk.isLoaded() && chunk.isGenerated()) {
						if (zone.inZone(chunk)) {
							plugin.infoMsg(InfoLevel.WARN, p, "이미 포함되어 있는 청크입니다.");
						} else {
							zone.enLarge(chunk);
						}
					}
				}
				case "delete" -> {
					if (zone.inZone(chunk)) {
						zone.shrink(chunk);
						plugin.infoMsg(InfoLevel.INFO, p, "해당 위치의 구역을 제외하였습니다.");
					} else {
						plugin.infoMsg(InfoLevel.WARN, p, "해당 구역에 포함되지않은 청크입니다.");
					}
				}
				default -> plugin.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 명령어입니다.");
			}
		} else {
			plugin.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 명령어입니다.");
		}
		return true;
	}

	//zone expand/delete/get/spawn/despawn/alive <type>

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		List<String> result = new ArrayList<>();

		if (args.length == 1) {
			List<String> subs = List.of("expand", "delete", "get", "spawn", "despawn", "alive");
			subs.stream().filter(sub -> sub.startsWith(args[0].toLowerCase())).forEach(result::add);
		}

		if (args.length == 2) Arrays.stream(ZoneType.values())
					.map(Enum::name)
					.filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
					.forEach(result::add);
		return result;
	}
}
