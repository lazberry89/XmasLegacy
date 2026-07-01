package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.*;
import org.lazberry.xmaslegacy.PluginUtils.ServerType;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.FirstRoleManager;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.SecondRoleManager;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Reflections {
	private static final @NotNull String packageName = "org/lazberry/xmaslegacy";
	
	private static @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

	/**
	 * Essential Exceptions : TASKS_OFF(onEnable), TASKS_ON(onDisable)
	 * - Always Excepted type is excepted.
	 * @param classPath Plugin ClassPath instance
	 * @param exceptions types of InitializeType to be excluded from invocation
	 */
	@Reflection(type = InitializeType.EXCEPTED)
	public static void invokeReflections(@NotNull ClassPath classPath, InitializeType ...exceptions) {
		var methods = Reflections.class.getDeclaredMethods();

		List<InitializeType> exceptionList = Arrays.asList(exceptions);

		for (Method method : methods) {
			if (!method.isAnnotationPresent(Reflection.class)) continue;

			Reflection annotation = method.getAnnotation(Reflection.class);
			if (annotation.type().equals(InitializeType.EXCEPTED)) continue;
			if (exceptionList.contains(annotation.type())) continue;

			try {
				method.setAccessible(true);
				method.invoke(null, classPath);
				log.info("Register {} has been invoked", method.getName());
			} catch (Exception e) {
				log.error("Error occurred while invoking reflection for method {}", method.getName(), e);
			}
		}
	}

	@Reflection(type = InitializeType.LISTENERS)
	public static void registerListeners(@NotNull ClassPath classPath) {
		try {
			for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
				Class<?> clazz = classInfo.load();

				if (!Listener.class.isAssignableFrom(clazz)) continue;
				if (!clazz.isAnnotationPresent(Listeners.class)) continue;
				if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;

				var listenerInstance = clazz.getDeclaredConstructor().newInstance();
				Bukkit.getPluginManager().registerEvents((Listener) listenerInstance, plugin());

				log.info("Listener {} Automatically registered", clazz.getSimpleName());
			}
		} catch (Exception e) {
			log.error("Error occurred while registering all listeners", e);
		}
	}

	@Reflection(type = InitializeType.COMMANDS)
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
					var pluginCommand = plugin().getCommand(cmdName);
					if (pluginCommand == null) continue;

					pluginCommand.setExecutor((CommandExecutor) commandInstance);

					if (TabCompleter.class.isAssignableFrom(clazz))
						pluginCommand.setTabCompleter((TabCompleter) commandInstance);

					plugin().getSLF4JLogger().info("Command {} Automatically registered", cmdName);
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while registering all Commands/TabCompleter", e);
		}
	}

	@Reflection(type = InitializeType.REGISTER)
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
					log.warn("Class {} don't have default Constructor. Passing process", clazz.getSimpleName());
					continue;
				}

				switch (grade) {
					case 1 -> {
						if (instance instanceof AbstractFirstRole firstRole) {
							FirstRoleManager.INSTANCE.register(firstRole);
							log.info("FirstRole {} class registered.", clazz.getSimpleName());
						} else log.error("Class {} is not extending AbstractFirstRole.", clazz.getSimpleName());

					}
					case 2 -> {
						if (instance instanceof AbstractSecondRole secondRole) {
							SecondRoleManager.INSTANCE.register(secondRole);
							log.info("SecondaryRole {} class registered.", clazz.getSimpleName());
						} else log.error("Class {} is not extending AbstractSecondRole.", clazz.getSimpleName());
					}
					default ->log.error("Class {} have wrong value = {}", clazz.getSimpleName(), grade);
				}

			} catch (Exception e) {
				log.error("Error occurred while registering class {}", classInfo.getName(), e);
			}
		}
	}

	@Reflection(type = InitializeType.TASKS_ON)
	public static void startTasks(@NotNull ClassPath classPath) {
		taskReflection(classPath, true);
	}

	@Reflection(type = InitializeType.TASKS_OFF)
	public static void stopTasks(@NotNull ClassPath classPath) {
		taskReflection(classPath, false);
	}

	private static void taskReflection(@NotNull ClassPath classPath, boolean enable) {
		for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (!clazz.isAnnotationPresent(Task.class)) continue;
				if (!Tasks.class.isAssignableFrom(clazz)) continue;
				Task taskAnnotation = clazz.getAnnotation(Task.class);
				ServerType type = taskAnnotation.type();

				Tasks instance;
				if (clazz.isEnum()) {
					var field = clazz.getField("INSTANCE");
					instance = (Tasks) field.get(null);
					log.info("Successfully created Enum instance of Task {}", clazz.getSimpleName());
				} else {
					var constructor = clazz.getDeclaredConstructor();
					constructor.setAccessible(true);
					instance = (Tasks) constructor.newInstance();
					log.info("Successfully created instance of Task {}", clazz.getSimpleName());
				}

				if (ServerInitializer.getServerType(plugin()) == type) {
					if (enable) {
						instance.startTask(plugin());
						log.info("Task {} started successfully.", clazz.getSimpleName());
					} else {
						instance.stopTask();
						log.info("Task {} stopped successfully.", clazz.getSimpleName());
					}
				} else {
					log.info("Task {} skipped due to Invalid ServerType. EXPECTED: {}, ACTUAL: {}", clazz.getSimpleName(), type, ServerInitializer.getServerType(plugin()));
				}
			} catch (Exception e) {
				log.error("Error occurred while starting task {}, Passing process.", classInfo.getName(), e);
			}
		}
	}
}
