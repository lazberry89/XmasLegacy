package xmaslegacy.PluginUtils;

import org.jetbrains.annotations.NotNull;
import xmaslegacy.XmasLegacy;

public interface ServerInitializer {
	void setup(@NotNull XmasLegacy plugin);
	static @NotNull ServerType getServerType() {
		var plugin = XmasLegacy.getInstance();
		plugin.saveDefaultConfig();
		return ServerType.getServerType(plugin.getConfig().getString("server-type", "main"));
	}
	static void initiate(@NotNull XmasLegacy plugin) {
		plugin.saveDefaultConfig();
		ServerType serverType = ServerType.getServerType(plugin.getConfig().getString("server-type", ServerType.MAIN.str()));
		switch (serverType) {
			case MAIN -> {
				serverType.getInitializer().setup(plugin);
				plugin.registerReflection();
			}
			case LOBBY -> serverType.getInitializer().setup(plugin);
		}
	}
}
