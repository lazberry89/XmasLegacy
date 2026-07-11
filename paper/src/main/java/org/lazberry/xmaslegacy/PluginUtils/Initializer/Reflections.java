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
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.RoleManagers.RoleManager;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.PlayerSkills;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public final class Reflections {
	private static final Map<Class<?>, Tasks> ACTIVE_TASKS = new HashMap<>();
	private static final @NotNull String packageName = "org.lazberry.xmaslegacy";
	
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
	public static void registerSkills(@NotNull ClassPath classPath) {
		for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (!Skills.class.isAssignableFrom(clazz)) continue;
				if (!clazz.isAnnotationPresent(Skill.class)) continue;

				Skill skillAnnotation = clazz.getAnnotation(Skill.class);
				PlayerSkills name = skillAnnotation.type();

				Object instance;
				try {
					instance = clazz.getDeclaredConstructor().newInstance();
				} catch (NoSuchMethodException e) {
					log.warn("Failed to create instance of skill {}. Passing..", clazz.getSimpleName());
					continue;
				}
				if (instance instanceof Skills<?> skill) {
					SkillManager.INSTANCE.register(name, skill);
					log.info("Registered skill {}.", name.name());
				}
			} catch (Exception e) {
				log.error("Error occurred while registering all skills.", e);
			}
		}
	}

	@Reflection(type = InitializeType.REGISTER)
	public static void registerRoles(@NotNull ClassPath classPath) {
		for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (!clazz.isAnnotationPresent(Roles.class)) continue;
				if (!RoleClass.class.isAssignableFrom(clazz)) {
					log.error("Clazz {} is not implementing RoleClass.class but Using @Roles annotation.", clazz.getSimpleName());
					continue;
				}

				Object instance;
				try {
					instance = clazz.getDeclaredConstructor().newInstance();
				} catch (NoSuchMethodException e) {
					log.warn("Class {} don't have default Constructor. Passing process", clazz.getSimpleName());
					continue;
				}
				if (!(instance instanceof RoleClass roleClass)) {
					log.error("Filtered instance \"{}\" is not Valid.", instance);
					continue;
				}
				RoleManager.INSTANCE.register(roleClass);
				log.info("Successfully registered role {}", clazz.getSimpleName());
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
		ServerType current = ServerInitializer.getServerType(plugin());

		for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (!clazz.isAnnotationPresent(Task.class)) continue;
				if (!Tasks.class.isAssignableFrom(clazz)) continue;

				Task taskAnnotation = clazz.getAnnotation(Task.class);
				List<ServerType> typeList = Arrays.asList(taskAnnotation.type());

				if (!typeList.contains(current) && !(current.isRequiresGlobalInitializer() && typeList.contains(ServerType.GLOBAL))) {
					log.info("Task {} skipped due to Invalid ServerType. EXPECTED: {}, ACTUAL: {}",
							clazz.getSimpleName(), Arrays.toString(taskAnnotation.type()), current);
					continue;
				}

				Tasks instance;
				if (clazz.isEnum()) {
					var field = clazz.getField("INSTANCE");
					instance = (Tasks) field.get(null);
				} else {
					if (enable) {
						if (ACTIVE_TASKS.containsKey(clazz)) {
							instance = ACTIVE_TASKS.get(clazz);
						} else {
							var constructor = clazz.getDeclaredConstructor();
							constructor.setAccessible(true);
							instance = (Tasks) constructor.newInstance();
							ACTIVE_TASKS.put(clazz, instance);
							log.info("Successfully created and cached instance of Task {}", clazz.getSimpleName());
						}
					} else {
						instance = ACTIVE_TASKS.get(clazz);
						if (instance == null) {
							log.warn("Task {} instance not found for stopping. Skipping.", clazz.getSimpleName());
							continue;
						}
					}
				}

				if (enable) {
					instance.startTask(plugin());
					log.info("Task {} started successfully.", clazz.getSimpleName());
				} else {
					instance.stopTask();
					if (!clazz.isEnum()) ACTIVE_TASKS.remove(clazz);
					log.info("Task {} stopped successfully.", clazz.getSimpleName());
				}
			} catch (Exception e) {
				log.error("Error occurred while processing task {}, Passing process.", classInfo.getName(), e);
			}
		}
	}
}
