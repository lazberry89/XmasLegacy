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
}
