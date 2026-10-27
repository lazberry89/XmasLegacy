package org.lazberry.xmaslegacy.plugin;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;

public interface Tasks {

	/**
	 * Task Starting logic.
	 * @param plugin XmasLegacy instance
	 */
	void startTask(@NotNull XmasLegacy plugin);

	/**
	 * Task Stopping logic.
	 */
	void stopTask();
}
