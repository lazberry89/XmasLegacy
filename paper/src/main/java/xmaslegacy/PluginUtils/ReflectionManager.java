package xmaslegacy.PluginUtils;

import com.google.common.reflect.ClassPath;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Annotation.Listeners;
import xmaslegacy.Annotation.Reflection;
import xmaslegacy.Annotation.Roles;
import xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import xmaslegacy.RoleManagers.FirstRoleManager.FirstRoleManager;
import xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import xmaslegacy.RoleManagers.SecondaryRoleManager.SecondRoleManager;
import xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionManager {
	private static final @NotNull XmasLegacy plugin = XmasLegacy.getInstance();
	private static final @NotNull String packageName = "xmaslegacy";

	@Reflection(comment = "Listeners Registration")
	public static void registerListeners(@NotNull ClassPath classPath) {
		try {
			for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
				Class<?> clazz = classInfo.load();

				if (!Listener.class.isAssignableFrom(clazz)) continue;
				if (!clazz.isAnnotationPresent(Listeners.class)) continue;
				if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;

				var listenerInstance = clazz.getDeclaredConstructor().newInstance();
				Bukkit.getPluginManager().registerEvents((Listener) listenerInstance, plugin);

				plugin.getSLF4JLogger().info("Listener {} Automatically registered", clazz.getSimpleName());
			}
		} catch (Exception e) {
			plugin.getSLF4JLogger().error("Error occurred while registering all listeners", e);
		}
	}

	@Reflection(comment = "Commands Registration")
	public static void registerCommands(@NotNull ClassPath classPath) {
		try {
			for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
				Class<?> clazz = classInfo.load();

				if (!CommandExecutor.class.isAssignableFrom(clazz)) continue;
				if (!clazz.isAnnotationPresent(Commands.class)) continue;
				if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;

				Commands autoCommand = clazz.getAnnotation(Commands.class);

				List<String> allCommands = new ArrayList<>();
				allCommands.add(autoCommand.command());
				allCommands.addAll(Arrays.asList(autoCommand.aliases()));

				var commandInstance = clazz.getDeclaredConstructor().newInstance();

				for (String cmdName : allCommands) {
					var pluginCommand = plugin.getCommand(cmdName);
					if (pluginCommand == null) continue;

					pluginCommand.setExecutor((CommandExecutor) commandInstance);

					if (TabCompleter.class.isAssignableFrom(clazz))
						pluginCommand.setTabCompleter((TabCompleter) commandInstance);

					plugin.getSLF4JLogger().info("Command {} Automatically registered", cmdName);
				}
			}
		} catch (Exception e) {
			plugin.getSLF4JLogger().error("Error occurred while registering all Commands/TabCompleter", e);
		}
	}

	@Reflection(comment = "Role Registration")
	public static void registerAllRoles(@NotNull ClassPath classPath) {
		for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (!clazz.isAnnotationPresent(Roles.class)) continue;

				Roles rolesAnnotation = clazz.getAnnotation(Roles.class);
				int grade = rolesAnnotation.grade();

				Object instance;
				try {
					instance = clazz.getDeclaredConstructor().newInstance();
				} catch (NoSuchMethodException e) {
					plugin.getSLF4JLogger().warn("Class {} don't have default Constructor. Passing process", clazz.getSimpleName());
					continue;
				}

				switch (grade) {
					case 1 -> {
						if (instance instanceof AbstractFirstRole firstRole) {
							FirstRoleManager.INSTANCE.register(firstRole);
							plugin.getSLF4JLogger().info("FirstRole {} class registered.", clazz.getSimpleName());
						} else plugin.getSLF4JLogger().error("Class {} is not extending AbstractFirstRole.", clazz.getSimpleName());

					}
					case 2 -> {
						if (instance instanceof AbstractSecondRole secondRole) {
							SecondRoleManager.INSTANCE.register(secondRole);
							plugin.getSLF4JLogger().info("SecondaryRole {} class registered.", clazz.getSimpleName());
						} else plugin.getSLF4JLogger().error("Class {} is not extending AbstractSecondRole.", clazz.getSimpleName());
					}
					default -> plugin.getSLF4JLogger().error("Class {} have wrong value = {}", clazz.getSimpleName(), grade);
				}

			} catch (Exception e) {
				plugin.getSLF4JLogger().error("Error occurred while registering class {}", classInfo.getName(), e);
			}
		}
	}
}
