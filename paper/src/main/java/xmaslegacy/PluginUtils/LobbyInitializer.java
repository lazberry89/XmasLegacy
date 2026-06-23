package xmaslegacy.PluginUtils;

import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Lobby.LobbyCommands.LobbyCommand;
import xmaslegacy.Lobby.LobbyListener;
import xmaslegacy.Lobby.LobbyManager;
import xmaslegacy.XmasLegacy;

public class LobbyInitializer implements ServerInitializer {

	@Override
	public void setup(@NotNull XmasLegacy plugin) {
		plugin.getLogger().warning("Lobby 모드로 시작합니다.");
		plugin.getSLF4JLogger().warn("server-type = \"lobby\" 일치하지 않을 시에 config.yml을 수정하세요.");
		var lobbyManager = new LobbyManager();

		plugin.getServer().getPluginManager().registerEvents(new LobbyListener(lobbyManager), plugin);

		var lobbyCommand = new LobbyCommand(lobbyManager);
		PluginCommand lobby = plugin.getCommand("lobby");
		if (lobby != null) {
			lobby.setExecutor(lobbyCommand);
			lobby.setTabCompleter(lobbyCommand);
		} else {
			plugin.getSLF4JLogger().error("커맨드가 plugin.yml에 등록되지 않았습니다. \"lobby\"");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

	}
}
