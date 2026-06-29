package xmaslegacy;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.PluginUtils.Initializer.ReflectionManager;
import xmaslegacy.PluginUtils.Initializer.ServerInitializer;

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

	public void registerGlobalReflection() {}

	/**
	 * only called by ServerInitializer. EVER DON'T CALL ILLEGALLY
	 */
	public void registerMainReflection() {
		try {
			var classPath = ClassPath.from(this.getClassLoader());
			ReflectionManager.registerListeners(classPath);
			ReflectionManager.registerCommands(classPath);
			ReflectionManager.registerAllRoles(classPath);
		} catch (IOException e) {
			log.error("Failed to register components via reflection", e);
			this.getServer().broadcast(ColorUtils.chat(Alert.RED + " Failed to load main plugin System. Disabling plugin."));
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}
}