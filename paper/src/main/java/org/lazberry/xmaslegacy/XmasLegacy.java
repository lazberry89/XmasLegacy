package org.lazberry.xmaslegacy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.LazberryRegistryFramework;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Reflections;
import redis.clients.jedis.*;

@Slf4j
public final class XmasLegacy extends JavaPlugin {

	private volatile @Getter static XmasLegacy instance = null;

	public XmasLegacy() {
		instance = this;
	}

	/**
	 * all of enable logics are Separated to Initializers.
	 * Check ServerInitializer.
	 */
	@Override
	public void onEnable() {
		LazberryRegistryFramework.setDebug(true);
		LazberryRegistryFramework.boot(this, XmasLegacy.class);
		Reflections.runInitializers(true);
	}

	/**
	 * Separated into ServerInitializers. Check classes.
	 */
	@Override
	public void onDisable() {
		// First: cleanUp empties the container the initializers live in.
		Reflections.runInitializers(false);
		LazberryRegistryFramework.cleanUp(this, XmasLegacy.class);
	}
}