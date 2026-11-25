package org.lazberry.xmaslegacy.api.wep;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/** /web info | /web prereg [count] — reads xmaslegacy.xyz data through {@link WebApiClient}. Ops and console only. */
@Commands(command = "web")
// Only main has web.api_internal_key set, and WebApiClient refuses to start without it.
@Components.Include(type = ServerType.MAIN)
public class WebApiCommand implements CommandExecutor, TabCompleter {
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Seoul"));

	private final WebApiClient client;

	@Inject
	public WebApiCommand(WebApiClient client) {
		this.client = client;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!sender.isOp()) {
			send(sender, "&c권한이 없습니다.");
			return true;
		}
		String sub = args.length > 0 ? args[0].toLowerCase() : "";
		switch (sub) {
			case "info" -> info(sender);
			case "prereg" -> prereg(sender, args.length > 1 ? args[1] : "10");
			default -> {
				send(sender, "&e/web info &7- 웹 서버 정보 (사전예약 수, 주소 공개 여부)");
				send(sender, "&e/web prereg [개수] &7- 최근 사전예약자 목록 (최대 100)");
			}
		}
		return true;
	}

	private void info(CommandSender sender) {
		send(sender, "&7웹 API에서 정보를 불러오는 중...");
		client.getServerInfo().thenAccept(info -> {
			if (info == null) {
				send(sender, "&c웹 API 응답을 받지 못했습니다. 서버 로그를 확인하세요.");
				return;
			}
			send(sender, "&6&l[웹 서버 정보]");
			send(sender, "&7이름: &f" + info.name() + " &8| &7버전: &f" + info.version());
			send(sender, "&7웹 표시 접속자: &f" + info.onlineCount() + "명");
			send(sender, "&7사전예약: &a" + info.preregCount() + "명");
			send(sender, "&7주소 공개: " + (info.addressRevealed() ? "&a공개됨 &f(" + info.address() + ")" : "&c비공개"));
		});
	}

	private void prereg(CommandSender sender, String countArg) {
		int count;
		try {
			count = Math.clamp(Integer.parseInt(countArg), 1, 100);
		} catch (NumberFormatException e) {
			send(sender, "&c개수는 숫자로 입력하세요. 예: /web prereg 20");
			return;
		}
		send(sender, "&7사전예약자 목록을 불러오는 중...");
		WebApiClient.getPreregisteredPlayers(count).whenComplete((players, ex) -> {
			if (ex != null) {
				send(sender, "&c목록을 읽지 못했습니다. API 응답 형식이 바뀌었을 수 있습니다. 서버 로그를 확인하세요.");
				return;
			}
			if (players.isEmpty()) {
				send(sender, "&7사전예약자가 없거나 웹 API에 연결하지 못했습니다.");
				return;
			}
			send(sender, "&6&l[사전예약자 " + players.size() + "명]");
			for (PreregisteredPlayer p : players) {
				send(sender, "&f" + p.name() + " &8- &7" + DATE.format(Instant.ofEpochMilli(p.registeredAt())));
			}
		});
	}

	private static void send(CommandSender sender, String message) {
		sender.sendMessage(ColorUtils.chat(message));
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!sender.isOp() || args.length != 1) return List.of();
		return Stream.of("info", "prereg").filter(s -> s.startsWith(args[0].toLowerCase())).toList();
	}
}
