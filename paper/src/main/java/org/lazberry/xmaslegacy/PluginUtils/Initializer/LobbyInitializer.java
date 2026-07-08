package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyCommands.LobbyCommand;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.ServerJoinListener;
import org.lazberry.xmaslegacy.XmasLegacy;

@Slf4j
public class LobbyInitializer implements ServerInitializer {

	@Override
	public void enable(@NotNull XmasLegacy plugin) {
		log.warn("Lobby 모드로 시작합니다.");
		log.warn("server-type = \"lobby\" 일치하지 않을 시에 config.yml을 수정하세요.");
		Bukkit.getPluginManager().registerEvents(new ServerJoinListener(), plugin);
		var lobbyManager = new LobbyManager();

		var lobbyCommand = new LobbyCommand(lobbyManager);
		PluginCommand lobby = plugin.getCommand("lobby");
		if (lobby != null) {
			lobby.setExecutor(lobbyCommand);
			lobby.setTabCompleter(lobbyCommand);
		} else {
			log.error("커맨드가 plugin.yml에 등록되지 않았습니다. \"lobby\"");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}

	@Override
	public void disable(@NotNull XmasLegacy plugin) {
		ServerInitializer.super.disable(plugin);
	}
}
