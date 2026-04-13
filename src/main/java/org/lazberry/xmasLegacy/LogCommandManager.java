package org.lazberry.xmasLegacy;

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
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class LogCommandManager implements CommandExecutor, TabCompleter {
    private final InquiryManager IM;
    private final XmasLegacy plugin;

    public LogCommandManager(InquiryManager IM, XmasLegacy plugin) {
        this.IM = IM;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
        if (!(commandSender instanceof Player p)) return false;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 로그를 볼 수 있는 권한이 없습니다."));
            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            return true;
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
	            case "inquiry" -> {
					String targetName = args[1]; // 람다 내부에서 쓰기 위해 변수 고정
	                p.sendMessage(ColorUtils.chat("&7로그를 불러오는 중입니다...")); // 사용자 경험(UX) 배려

	                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
		                OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
		                UUID uuid = op.getUniqueId();

		                List<String> logs = IM.getInquiryLogs(uuid);

		                if (logs.isEmpty() || (logs.size() == 1 && logs.getFirst().contains("없습니다"))) {
			                p.sendMessage(ColorUtils.chat(Prefix.RED + " '" + targetName + "' 유저의 기록이 없습니다."));

		                } else {
							logs.forEach(p::sendMessage);
		                }
	                });
	            }
                default -> {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 명령어가 아닙니다."));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
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
						Component comp = ComponentChanger.comp("&e- &f" + userName + " &7: " + msg + " ")
								.append(ComponentChanger.comp("&a&l[이동]"))
								.clickEvent(ClickEvent.runCommand("/이동문의 " + userName));

						p.sendMessage(comp);
					}
				}
				default -> {
					p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 명령어가 아닙니다."));
					p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
				}
			}
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
	    List<String> completions = new ArrayList<>();
	    if (args.length == 1) {
		    completions.add("inquiry");
		    completions.add("inquiries");
	    } else if (args.length == 2 && args[0].equalsIgnoreCase("inquiry")) {
		    // 현재 접속 중인 플레이어 이름을 추천해줌
		    for (Player onlineP : Bukkit.getOnlinePlayers()) {
			    completions.add(onlineP.getName());
		    }
	    }

	    // 사용자가 입력 중인 글자로 시작하는 것만 필터링 (보통 필수)
	    String lastArg = args[args.length - 1];
	    return completions.stream()
			    .filter(s -> s.toLowerCase().startsWith(lastArg.toLowerCase()))
			    .toList();
    }
}
