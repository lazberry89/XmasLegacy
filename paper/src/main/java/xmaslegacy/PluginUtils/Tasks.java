package xmaslegacy.PluginUtils;

import org.jetbrains.annotations.NotNull;
import xmaslegacy.XmasLegacy;

public interface Tasks {
	void startTask(@NotNull XmasLegacy plugin);
	void stopTask();
}
