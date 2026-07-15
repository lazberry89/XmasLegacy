package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.*;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.RoleManagers.RoleManager;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Manager;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerManager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public final class Reflections {
	private static final @NotNull Map<Class<?>, Object> BEAN_CONTAINER = new HashMap<>();
	private static final @NotNull List<ServerManager> ORDERED_MANAGERS = new ArrayList<>();
	private static final @NotNull Map<Class<?>, Tasks> ACTIVE_TASKS = new HashMap<>();
	private static final @NotNull String packageName = "org.lazberry.xmaslegacy";

	private static @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

	@Reflection(type = InitializeType.EXCEPTED)
	public static void invokeReflections(@NotNull ClassPath classPath, InitializeType... exceptions) {
		// 1. 컨테이너 인스턴스화
		buildBeanContainer(classPath);

		// 2. 의존성 주입 (@Manager 및 @Plugin 필드 스캔 후 꽂아넣기)
		injectDependencies();

		// 3. ServerManager 분류 및 우선순위 자동 계산 정렬
		sortAndRegisterManagers();

		// 4. 순서에 따른 일괄 init() 호출
		initializeManagers();

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
	 * 패키지 내의 모든 클래스를 스캔하여 대상 객체들을 인스턴스화하여 컨테이너에 보관합니다.
	 */
	private static void buildBeanContainer(@NotNull ClassPath classPath) {
		log.info("[IoC] Building Bean Container and instantiating components...");

		for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

				// @Registry 또는 기존 대상 어노테이션이 감지되면 인스턴스화 대상으로 지정
				if (clazz.isAnnotationPresent(Listeners.class) ||
						clazz.isAnnotationPresent(Commands.class) ||
						clazz.isAnnotationPresent(Skill.class) ||
						clazz.isAnnotationPresent(Roles.class) ||
						clazz.isAnnotationPresent(Inject.class) ||
						clazz.isAnnotationPresent(Registry.class)) {

					Object instance;
					if (clazz.isEnum()) {
						// Enum 클래스인 경우 INSTANCE 상수를 가져옴
						Field instanceField = clazz.getDeclaredField("INSTANCE");
						instance = instanceField.get(null);
					} else {
						var constructor = clazz.getDeclaredConstructor();
						constructor.setAccessible(true);
						instance = constructor.newInstance();
					}

					if (instance != null) {
						BEAN_CONTAINER.put(clazz, instance);
					}
				}
			} catch (NoSuchFieldException | NoSuchMethodException ignored) {
			} catch (Exception e) {
				log.error("[IoC] Failed to instantiate managed bean: {}", classInfo.getName(), e);
			}
		}
	}

	/**
	 * 모든 빈을 탐색하며 @Manager 및 @Plugin 필드 주입을 일괄 처리합니다.
	 */
	private static void injectDependencies() {
		log.info("[IoC] Injecting field dependencies...");
		var plugin = plugin();

		for (Object instance : BEAN_CONTAINER.values()) {
			Class<?> clazz = instance.getClass();

			if (!clazz.isAnnotationPresent(Inject.class)) {
				continue;
			}

			while (clazz != null && clazz != Object.class) {
				for (Field field : clazz.getDeclaredFields()) {

					if (field.isAnnotationPresent(Plugin.class)) {
						if (field.getType().isAssignableFrom(plugin.getClass())) {
							field.setAccessible(true);
							try {
								if (Modifier.isStatic(field.getModifiers())) {
									field.set(null, plugin);
								} else {
									field.set(instance, plugin);
								}
							} catch (IllegalAccessException e) {
								log.error("[IoC] Failed to inject plugin into {}#{}", clazz.getSimpleName(), field.getName(), e);
							}
						}
					}

					if (field.isAnnotationPresent(Manager.class)) {
						Class<?> fieldType = field.getType();
						Object managerInstance = BEAN_CONTAINER.get(fieldType);

						if (managerInstance != null) {
							field.setAccessible(true);
							try {
								if (Modifier.isStatic(field.getModifiers())) {
									field.set(null, managerInstance);
								} else {
									field.set(instance, managerInstance);
								}
								log.debug("[IoC] Injected @Manager '{}' into {}#{}", fieldType.getSimpleName(), clazz.getSimpleName(), field.getName());
							} catch (IllegalAccessException e) {
								log.error("[IoC] Failed to inject @Manager into {}#{}", clazz.getSimpleName(), field.getName(), e);
							}
						} else {
							log.warn("[IoC] Cannot find @Registry bean for type: {} (Required by {}#{})",
									fieldType.getSimpleName(), clazz.getSimpleName(), field.getName());
						}
					}
				}
				clazz = clazz.getSuperclass();
			}
		}

		injectGlobalStaticFields(plugin);
	}

	/**
	 * ServerManager 구현체들을 분류하고, 의존성 개수에 맞추어 정렬하여 리스트에 등록합니다.
	 */
	private static void sortAndRegisterManagers() {
		log.info("[IoC] Sorting ServerManagers by auto-calculated priority...");
		List<ServerManager> tempManagers = new ArrayList<>();

		for (var entry : BEAN_CONTAINER.entrySet()) {
			Class<?> clazz = entry.getKey();

			if (clazz.isAnnotationPresent(Registry.class) && ServerManager.class.isAssignableFrom(clazz)) {
				tempManagers.add((ServerManager) entry.getValue());
			}
		}

		tempManagers.sort(Comparator.comparingInt(manager -> {
			Class<?> clazz = manager.getClass();
			int managerFieldCount = 0;

			if (clazz.isAnnotationPresent(Inject.class)) {
				while (clazz != null && clazz != Object.class) {
					for (Field field : clazz.getDeclaredFields()) {
						if (field.isAnnotationPresent(Manager.class)) {
							managerFieldCount++;
						}
					}
					clazz = clazz.getSuperclass();
				}
			}
			return managerFieldCount + 1;
		}));

		ORDERED_MANAGERS.clear();
		ORDERED_MANAGERS.addAll(tempManagers);
	}

	/**
	 * 정렬된 매니저들의 init() 메서드를 순차적으로 호출합니다.
	 */
	private static void initializeManagers() {
		log.info("[IoC] Initializing ServerManagers sequentially...");
		for (ServerManager manager : ORDERED_MANAGERS) {
			try {
				manager.init();
				log.info("[IoC] Initialized ServerManager: {}", manager.getClass().getSimpleName());
			} catch (Exception e) {
				log.error("[IoC] Failed to initialize ServerManager: {}", manager.getClass().getSimpleName(), e);
			}
		}
	}

	private static void injectGlobalStaticFields(@NotNull XmasLegacy plugin) {
		for (var entry : BEAN_CONTAINER.entrySet()) {
			Class<?> clazz = entry.getKey();
			for (Field field : clazz.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Plugin.class)) continue;
				field.setAccessible(true);

				try {
					if (Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(plugin.getClass())) {
						field.set(null, plugin);
						continue;
					}

					if (clazz.isEnum()) {
						Object enumInstance = entry.getValue();
						if (enumInstance != null && field.getType().isAssignableFrom(plugin.getClass())) {
							field.set(enumInstance, plugin);
						}
					}
				} catch (Exception ignored) {}
			}
		}
	}

	@Deprecated
	private static void injectGlobalStaticFields(@NotNull ClassPath classPath, @NotNull XmasLegacy plugin) {

	}

    /* ==========================================
       이하 Bukkit Event 및 Command 자동 등록 메서드 영역 (이전과 동일)
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