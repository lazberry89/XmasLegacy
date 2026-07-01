package org.lazberry.xmaslegacy.PluginUtils;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;

public interface Tasks {
	void startTask(@NotNull XmasLegacy plugin);
	void stopTask();
}
