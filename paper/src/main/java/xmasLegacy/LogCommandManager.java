package xmasLegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Region.Region;
import xmasLegacy.Region.RegionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogCommandManager implements CommandExecutor, TabCompleter {
    private final InquiryManager IM;
    private final XmasLegacy plugin;
	private final RegionManager RM;

    public LogCommandManager() {
        this.IM = InquiryManager.getInstance();
        this.plugin = XmasLegacy.getInstance();
		this.RM = RegionManager.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
        if (!(commandSender instanceof Player p)) return false;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 로그를 볼 수 있는 권한이 없습니다."));
            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            return true;
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
	            case "inquiry" -> {
					String targetName = args[1];
	                p.sendMessage(ColorUtils.chat("&7로그를 불러오는 중입니다..."));

	                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
		                OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
		                UUID uuid = op.getUniqueId();

		                List<String> logs = IM.getInquiryLogs(uuid);

		                if (logs.isEmpty() || (logs.size() == 1 && logs.getFirst().contains("없습니다"))) {
			                p.sendMessage(ColorUtils.chat(Alert.RED + "'" + targetName + "' 유저의 기록이 없습니다."));

		                } else {
							logs.forEach(p::sendMessage);
		                }
	                });
	            }
	            case "regions" -> {
		            OfflinePlayer of = Bukkit.getOfflinePlayer(args[1]);
					if (of.hasPlayedBefore()) {
						List<Region> regions = RM.getRegion(of.getUniqueId());
						if (regions == null || regions.isEmpty()) {
							p.sendMessage(ColorUtils.chat(Alert.RED + " 구역이 없습니다."));
							return true;
						}
						SendRegions(p, regions);
					} else {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 유저가 존재하지 않습니다."));
						p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
						return true;
					}
	            }
                default -> {
                    p.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 명령어가 아닙니다."));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
					return true;
                }
            }
        } else if (args.length == 1) {
			switch (args[0]) {
				case "inquiries" -> {
					p.sendMessage(ColorUtils.chat("&b&l[현재 대기 중인 문의 목록]"));

					if (IM.getInquiryMap().isEmpty()) {
						p.sendMessage(ColorUtils.chat("&7대기 중인 문의가 없습니다. 평화롭네요!"));
						return true;
					}

					for (Map.Entry<UUID, String> entry : IM.getInquiryMap().entrySet()) {
						String userName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
						String msg = entry.getValue();

						// 채팅창에 클릭 가능한 메시지로 띄워줌
						Component comp = ColorUtils.chat(String.format("&e- &f%s &7: %s ", userName, msg))
								.append(ColorUtils.chat("&a&l[이동]"))
								.clickEvent(ClickEvent.runCommand("/이동문의 " + userName));

						p.sendMessage(comp);
					}
				}
				case "regions" -> {
					List<Region> regions = RM.getRegions();
					if (regions.isEmpty()) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 구역이 없습니다."));
						return true;
					}
					SendRegions(p, regions);
				}
				default -> {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 명령어가 아닙니다."));
					p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
				}
			}
        }
        return true;
    }

	private void SendRegions(Player p, List<Region> regions) {
		for (Region region : regions) {
			p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
			p.sendMessage(ColorUtils.chat("&6&lRegion ID : &f" + region.getId()));
			p.sendMessage(ColorUtils.chat("&eOwner : &f" + region.getName()));

			int x = region.getCenter().getBlockX();
			int y = region.getCenter().getBlockY();
			int z = region.getCenter().getBlockZ();
			String world = region.getCenter().getWorld().getName();

			p.sendMessage(ColorUtils.chat(String.format("&eLocation : &7%s (%d, %d, %d)", world, x, y, z)));

			String entry = region.isAllowPublicEntry() ? "&a허용" : "&c차단";
			String interact = region.isAllowPublicInteraction() ? "&a허용" : "&c차단";
			p.sendMessage(ColorUtils.chat(String.format("&eSettings : &f출입[%s&f] 상호작용[%s&f]", entry, interact)));
		}
		p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			completions.add("inquiry");
			completions.add("inquiries");
			completions.add("regions");
		} else if (args.length == 2) {
			String sub = args[0].toLowerCase();
			if (sub.equals("inquiry") || sub.equals("regions")) {
				for (Player onlineP : Bukkit.getOnlinePlayers()) {
					completions.add(onlineP.getName());
				}
			}
		}

		String lastArg = args[args.length - 1];
		return completions.stream()
				.filter(s -> s.toLowerCase().startsWith(lastArg.toLowerCase()))
				.toList();
	}
}
