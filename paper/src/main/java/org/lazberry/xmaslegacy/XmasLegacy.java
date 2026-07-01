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
	 * all of enable logics are Seperated to Initializers.
	 * Check ServerInitializer.
	 */
	@Override
	public void onEnable() {
		ServerInitializer.initiate(this);
	}

	/**
	 * Seperated into ServerInitializers. Check classes.
	 */
	@Override
	public void onDisable() {
		ServerInitializer.shutdown(this);
	}

	public void registerReflection() {
		try {
			ClassPath classPath = ClassPath.from(getClassLoader());
			Reflections.invokeReflections(classPath, InitializeType.TASKS_OFF);
		} catch (IOException e) {
			log.error("Failed to initialize framework.");
		}
	}

	public void unregisterReflection() {
		try {
			ClassPath classPath = ClassPath.from(getClassLoader());
			Reflections.invokeReflections(classPath,
					InitializeType.TASKS_ON,
					InitializeType.REGISTER,
					InitializeType.LISTENERS,
					InitializeType.COMMANDS);
		} catch (IOException e) {
			log.error("Failed to uninitialize framework.");
		}
	}
}