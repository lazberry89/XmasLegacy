package org.lazberry.xmaslegacy;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.InitializeType;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.Reflections;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.ServerInitializer;

import java.io.IOException;

@Slf4j
public final class XmasLegacy extends JavaPlugin {

	private @Getter static XmasLegacy instance;

	public XmasLegacy() {
		instance = this;
	}

	/**
	 * all of enable logics are Separated to Initializers.
	 * Check ServerInitializer.
	 */
	@Override
	public void onEnable() {
		this.registerReflection();
	}

	/**
	 * Separated into ServerInitializers. Check classes.
	 */
	@Override
	public void onDisable() {
		this.unregisterReflection();
	}

	/**
	 * Invokes all reflections without {@link InitializeType#TASKS_OFF}.
	 * @see Reflections
	 * @see InitializeType
	 * @see ServerInitializer#initiate(XmasLegacy)
	 */
	public void registerReflection() {
		try {
			ClassPath classPath = ClassPath.from(getClassLoader());
			Reflections.invokeReflections(classPath, InitializeType.TASKS_OFF);
			Reflections.runInitializers(true);
		} catch (IOException e) {
			log.error("Failed to initialize framework.");
		}
	}

	/**
	 * Invoke only {@link InitializeType#TASKS_OFF} so that tasks Closing Reflection
	 * cleans tasks up.
	 * @see Reflections
	 * @see org.lazberry.xmaslegacy.PluginUtils.Tasks
	 * @see ServerInitializer#shutdown(XmasLegacy)
	 */
	public void unregisterReflection() {
		Reflections.stopTasks(null);
		Reflections.runInitializers(false);
	}
}