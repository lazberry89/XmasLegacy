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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public final class Reflections {
	private static final @NotNull Map<Class<?>, Object> BEAN_CONTAINER = new HashMap<>();
	private static final @NotNull Map<Class<?>, Tasks> ACTIVE_TASKS = new HashMap<>();
	private static final @NotNull String packageName = "org.lazberry.xmaslegacy";

	private static @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

	/**
	 * reflections 구동의 최우선 진입점.
	 * Bean 컨테이너를 먼저 구축하고 주입한 뒤, 기존 리플렉션 등록 메서드들을 호출합니다.
	 */
	@Reflection(type = InitializeType.EXCEPTED)
	public static void invokeReflections(@NotNull ClassPath classPath, InitializeType... exceptions) {
		buildBeanContainer(classPath);

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

	/**
	 * 패키지 내의 모든 클래스를 스캔하여 대상 객체들을 단 한 번만 인스턴스화하고,
	 * 필드 의존성(@Plugin)을 주입하여 컨테이너에 보관합니다.
	 */
	private static void buildBeanContainer(@NotNull ClassPath classPath) {
		log.info("[IoC] Building Bean Container and injecting dependencies...");

		for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum()) continue;

				if (clazz.isAnnotationPresent(Listeners.class) ||
						clazz.isAnnotationPresent(Commands.class) ||
						clazz.isAnnotationPresent(Skill.class) ||
						clazz.isAnnotationPresent(Roles.class) ||
						clazz.isAnnotationPresent(Inject.class)) {

					var constructor = clazz.getDeclaredConstructor();
					constructor.setAccessible(true);
					Object instance = constructor.newInstance();

					BEAN_CONTAINER.put(clazz, instance);
				}
			} catch (NoSuchMethodException ignored) {
			} catch (Exception e) {
				log.error("[IoC] Failed to instantiate managed bean: {}", classInfo.getName(), e);
			}
		}

		var plugin = plugin();
		for (Object instance : BEAN_CONTAINER.values()) {
			injectPluginField(instance, plugin);
		}
		injectGlobalStaticFields(classPath, plugin);
		log.info("[IoC] Bean Container built successfully. (Total Managed Beans: {})", BEAN_CONTAINER.size());
	}

	private static void injectPluginField(@NotNull Object instance, @NotNull XmasLegacy plugin) {
		Class<?> clazz = instance.getClass();

		while (clazz != null && clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Plugin.class)) continue;

				if (!field.getType().isAssignableFrom(plugin.getClass())) {
					log.error("[IoC] @Plugin injection failed. " +
									"Class \"{}\"'s field \"{}\" type is not compatible with {}",
							clazz.getSimpleName(), field.getName(), plugin.getClass().getSimpleName());
					continue;
				}

				field.setAccessible(true);
				try {
					field.set(instance, plugin);
					log.debug("[IoC] Successfully injected plugin into {}#{}", clazz.getSimpleName(), field.getName());
				} catch (IllegalAccessException e) {
					log.error("[IoC] Failed to inject plugin into {}#{}", clazz.getSimpleName(), field.getName(), e);
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	private static void injectGlobalStaticFields(@NotNull ClassPath classPath, @NotNull XmasLegacy plugin) {
		for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				for (Field field : clazz.getDeclaredFields()) {
					if (!field.isAnnotationPresent(Plugin.class)) continue;
					field.setAccessible(true);

					if (Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(plugin.getClass())) {
						field.set(null, plugin);
						continue;
					}

					if (clazz.isEnum()) {
						Field instanceField = clazz.getDeclaredField("INSTANCE");
						Object enumInstance = instanceField.get(null);
						if (enumInstance != null && field.getType().isAssignableFrom(plugin.getClass())) {
							field.set(enumInstance, plugin);
						}
					}
				}
			} catch (Exception ignored) {}
		}
	}

    /* ==========================================
       아래 등록 메서드들은 이제 직접 newInstance()를 하지 않고,
       중앙 Bean 컨테이너에서 이미 조립이 완료된 싱글톤 객체들을 꺼내서 "등록만" 해줍니다. (SRP 준수)
       ========================================== */

	@Reflection(type = InitializeType.LISTENERS)
	public static void registerListeners(@NotNull ClassPath classPath) {
		try {
			for (var entry : BEAN_CONTAINER.entrySet()) {
				Class<?> clazz = entry.getKey();
				if (Listener.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Listeners.class)) {
					Listener instance = (Listener) entry.getValue();
					Bukkit.getPluginManager().registerEvents(instance, plugin());
					log.info("Listener {} Automatically registered from IoC Container", clazz.getSimpleName());
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while registering all listeners", e);
		}
	}

	@Reflection(type = InitializeType.COMMANDS)
	public static void registerCommands(@NotNull ClassPath classPath) {
		try {
			for (var entry : BEAN_CONTAINER.entrySet()) {
				Class<?> clazz = entry.getKey();
				if (CommandExecutor.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Commands.class)) {
					Commands autoCommand = clazz.getAnnotation(Commands.class);

					List<String> allCommands = new ArrayList<>();
					allCommands.add(autoCommand.command());
					allCommands.addAll(Arrays.asList(autoCommand.aliases()));

					Object commandInstance = entry.getValue();

					for (String cmdName : allCommands) {
						var pluginCommand = plugin().getCommand(cmdName);
						if (pluginCommand == null) continue;

						pluginCommand.setExecutor((CommandExecutor) commandInstance);

						if (TabCompleter.class.isAssignableFrom(clazz))
							pluginCommand.setTabCompleter((TabCompleter) commandInstance);

						plugin().getSLF4JLogger().info("Command {} Automatically registered from IoC Container", cmdName);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while registering all Commands/TabCompleter", e);
		}
	}

	@Reflection(type = InitializeType.REGISTER)
	public static void registerSkills(@NotNull ClassPath classPath) {
		for (var entry : BEAN_CONTAINER.entrySet()) {
			Class<?> clazz = entry.getKey();
			if (Skills.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Skill.class)) {
				Skill skillAnnotation = clazz.getAnnotation(Skill.class);
				PlayerSkills name = skillAnnotation.type();

				Skills<?> skill = (Skills<?>) entry.getValue();
				SkillManager.INSTANCE.register(name, skill);
				log.info("Registered skill {} from IoC Container.", name.name());
			}
		}
	}

	@Reflection(type = InitializeType.REGISTER)
	public static void registerRoles(@NotNull ClassPath classPath) {
		for (var entry : BEAN_CONTAINER.entrySet()) {
			Class<?> clazz = entry.getKey();
			if (RoleClass.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Roles.class)) {
				RoleClass roleClass = (RoleClass) entry.getValue();
				RoleManager.INSTANCE.register(roleClass);
				log.info("Successfully registered role {} from IoC Container", clazz.getSimpleName());
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