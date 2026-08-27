package org.lazberry.xmaslegacy;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.LazberryRegistryFramework;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Reflections;

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
		LazberryRegistryFramework.setDebug(false);
		LazberryRegistryFramework.boot(this, XmasLegacy.class);
		Reflections.runInitializers(true);
	}

	/**
	 * Separated into ServerInitializers. Check classes.
	 */
	@Override
	public void onDisable() {
		LazberryRegistryFramework.cleanUp(this, XmasLegacy.class);
		Reflections.runInitializers(false);
	}
}