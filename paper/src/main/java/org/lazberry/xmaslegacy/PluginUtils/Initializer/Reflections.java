package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Reflection;
import org.lazberry.xmaslegacy.Annotation.Roles;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Initializers;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.RoleManagers.RoleManager;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerManager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public final class Reflections {
	private static final @NotNull Map<Class<?>, Object> BEAN_CONTAINER = new LinkedHashMap<>();
	private static final @NotNull Set<Class<?>> CONSTRUCTION_STACK = new HashSet<>();
	private static final @NotNull List<ServerManager> ORDERED_MANAGERS = new ArrayList<>();
	private static final @NotNull String packageName = "org.lazberry.xmaslegacy";

	public static void runInitializers(boolean on) {
		log.info("[IoC] Starting Server Initializers...");
		try {
			Initializers initializersBean = (Initializers) BEAN_CONTAINER.get(Initializers.class);
			if (initializersBean == null) {
				log.warn("[IoC] Initializers bean not found in container! Skipping lifecycle triggers.");
				return;
			}

			ServerType current = ServerInitializer.getServerType(plugin());

			if (current.isRequiresGlobalInitializer()) {
				ServerInitializer globalInit = initializersBean.getInitializer(ServerType.GLOBAL);
				log.info("[Lifecycle] Executing GlobalInitializer...");
				if (on) globalInit.initiate(plugin());
				else globalInit.shutdown(plugin());
			}

			ServerInitializer specificInit = initializersBean.getInitializer(current);
			log.info("[Lifecycle] Executing {}Initializer...", current.name());
			if (on) specificInit.initiate(plugin());
			else specificInit.shutdown(plugin());

		} catch (Exception e) {
			log.error("[Lifecycle] CRITICAL | Failed to execute initializers", e);
		}
	}

	private static @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

	public static void registerInstance(@NotNull Class<?> clazz, @NotNull Object instance) {
		BEAN_CONTAINER.put(clazz, instance);
		log.info("[IoC] Pre-registered external bean: {}", clazz.getSimpleName());
	}

	@Reflection(type = InitializeType.EXCEPTED)
	public static void invokeReflections(@NotNull ClassPath classPath, InitializeType... exceptions) {
		registerInstance(XmasLegacy.class, plugin());
		registerInstance(JavaPlugin.class, plugin());
		registerInstance(File.class, plugin().getDataFolder());

		buildAndInjectBeans(classPath);
		collectManagers();
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

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isCompatibleWithCurrentServer(@NotNull Class<?> clazz) {
		ServerType current = ServerInitializer.getServerType(plugin());

		// 1. @Registry.Exclude 검사 (커맨드, 매니저 가리지 않고 전체 적용)
		if (clazz.isAnnotationPresent(Registry.Exclude.class)) {
			Registry.Exclude exclude = clazz.getAnnotation(Registry.Exclude.class);
			for (ServerType excludedType : exclude.type()) {
				if (excludedType == current) return false;
			}
		}

		// 2. @Registry.Include 검사 (커맨드, 매니저 가리지 않고 전체 적용)
		if (clazz.isAnnotationPresent(Registry.Include.class)) {
			Registry.Include include = clazz.getAnnotation(Registry.Include.class);
			boolean matched = false;
			for (ServerType targetType : include.type()) {
				if (!isServerTypeUnCompatible(targetType, current)) {
					matched = true;
					break;
				}
			}
			if (!matched) return false;
		}

		// 💡 원래 있던 @Registry.Command 내부의 구형 type() 검사는 깔끔하게 제거했습니다!
		// 이제 커맨드도 위 Include/Exclude 설정을 그대로 따라갑니다.

		// 3. @Task 검사
		if (clazz.isAnnotationPresent(Task.class)) {
			Task taskAnnotation = clazz.getAnnotation(Task.class);
			List<ServerType> typeList = Arrays.asList(taskAnnotation.type());

			return typeList.contains(current) || (current.isRequiresGlobalInitializer() && typeList.contains(ServerType.GLOBAL));
		}

		return true;
	}

	private static boolean isServerTypeUnCompatible(@NotNull ServerType targetType, @NotNull ServerType currentType) {
		if (targetType == currentType) return false;
		return targetType != ServerType.GLOBAL || !currentType.isRequiresGlobalInitializer();
	}

	private static void buildAndInjectBeans(@NotNull ClassPath classPath) {
		log.info("[IoC] Building Bean Container and resolving constructor dependencies...");

		List<Class<?>> targetClasses = new ArrayList<>();
		for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			try {
				Class<?> clazz = classInfo.load();
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

				if (clazz.isAnnotationPresent(org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass.class)) {
					log.debug("[IoC] Skipping @ConsumableClass: {}", clazz.getSimpleName());
					continue;
				}

				// 1차 관문: 호환성 필터 (여기서 걸러지면 인스턴스화 대상에서 완전히 제외됨)
				if (!isCompatibleWithCurrentServer(clazz)) {
					log.debug("[IoC] Skipping incompatible class: {}", clazz.getSimpleName());
					continue;
				}

				boolean hasInjectConstructor = false;
				for (var constructor : clazz.getDeclaredConstructors()) {
					if (constructor.isAnnotationPresent(Inject.class)) {
						hasInjectConstructor = true;
						break;
					}
				}

				// 수집 대상 조건 설정
				if (clazz.isAnnotationPresent(Registry.Include.class) ||
						clazz.isAnnotationPresent(Registry.Exclude.class) ||
						clazz.isAnnotationPresent(Registry.Command.class) ||
						ServerManager.class.isAssignableFrom(clazz) || // 💡 명시적 어노테이션이 없는 일반 매니저도 안전하게 스캔 대상에 포함
						clazz.isAnnotationPresent(Skill.class) ||
						clazz.isAnnotationPresent(Roles.class) ||
						clazz.isAnnotationPresent(Task.class) ||
						hasInjectConstructor) {

					targetClasses.add(clazz);
				}
			} catch (Exception e) {
				log.warn("[IoC] Failed to load class for scanning: {}", classInfo.getName());
			}
		}

		for (Class<?> targetClass : targetClasses) {
			try {
				getOrCreateBean(targetClass);
			} catch (Exception e) {
				log.error("[IoC] CRITICAL | Failed to instantiate bean: {}", targetClass.getSimpleName(), e);
			}
		}
	}

	private static @Nullable Object getOrCreateBean(@NotNull Class<?> clazz) throws Exception {
		for (var entry : BEAN_CONTAINER.entrySet()) {
			if (clazz.isAssignableFrom(entry.getKey())) {
				return entry.getValue();
			}
		}

		if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			Class<?> implementationClass = findImplementation(clazz);
			if (implementationClass == null) {
				throw new NoSuchElementException("[IoC] Cannot find any registered implementation for: " + clazz.getName());
			}
			return getOrCreateBean(implementationClass);
		}

		if (!isCompatibleWithCurrentServer(clazz)) {
			throw new IllegalStateException("[IoC] " + clazz.getSimpleName() + " is NOT compatible with the current server type!");
		}

		if (CONSTRUCTION_STACK.contains(clazz)) {
			throw new IllegalStateException("Circular dependency detected involving: " + clazz.getSimpleName());
		}

		CONSTRUCTION_STACK.add(clazz);
		Object instance = null;

		try {
			if (clazz.isEnum()) {
				Field instanceField = clazz.getDeclaredField("INSTANCE");
				instance = instanceField.get(null);
			} else {
				Constructor<?> targetConstructor = null;
				for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
					if (constructor.isAnnotationPresent(Inject.class)) {
						targetConstructor = constructor;
						break;
					}
				}

				if (targetConstructor == null) {
					targetConstructor = clazz.getDeclaredConstructor();
				}

				targetConstructor.setAccessible(true);
				Class<?>[] paramTypes = targetConstructor.getParameterTypes();
				Object[] paramInstances = new Object[paramTypes.length];

				for (int i = 0; i < paramTypes.length; i++) {
					Class<?> paramType = paramTypes[i];
					paramInstances[i] = getOrCreateBean(paramType);
				}

				instance = targetConstructor.newInstance(paramInstances);
				log.debug("[IoC] Successfully created bean: {} with {} dependencies.", clazz.getSimpleName(), paramTypes.length);
			}

			if (instance != null) {
				BEAN_CONTAINER.put(clazz, instance);
			}

			return instance;

		} finally {
			CONSTRUCTION_STACK.remove(clazz);
		}
	}

	private static Class<?> findImplementation(Class<?> interfaceType) {
		try {
			ClassPath classPath = com.google.common.reflect.ClassPath.from(Reflections.class.getClassLoader());
			for (var classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
				Class<?> candidate = classInfo.load();
				if (candidate.isInterface() || Modifier.isAbstract(candidate.getModifiers())) continue;
				if (candidate.isAnnotationPresent(org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass.class)) {
					continue;
				}
				if (interfaceType.isAssignableFrom(candidate)) {
					if (candidate.isAnnotationPresent(Registry.Include.class) ||
							candidate.isAnnotationPresent(Registry.Exclude.class) ||
							candidate.isAnnotationPresent(Registry.Command.class) ||
							ServerManager.class.isAssignableFrom(candidate)) {
						return candidate;
					}
				}
			}
		} catch (Exception e) {
			log.error("[IoC] Error occurred while finding implementation for {}", interfaceType.getSimpleName(), e);
		}
		return null;
	}

	private static void collectManagers() {
		ORDERED_MANAGERS.clear();
		for (var entry : BEAN_CONTAINER.entrySet()) {
			Class<?> clazz = entry.getKey();

			// 💡 핵심 예술 포인트 변경:
			// 이미 BEAN_CONTAINER에 담겨있다는 것 자체가 '현재 서버 유형 검사'를 완벽히 통과했다는 뜻입니다.
			// 따라서 지저분하게 어노테이션 매칭 조건문을 주렁주렁 달 필요 없이, 오직 ServerManager 인터페이스를
			// 상속받았는지만 체크해서 쏙쏙 골라 담으면 끝납니다. 가독성 및 확장성 폭발!
			if (ServerManager.class.isAssignableFrom(clazz)) {
				ORDERED_MANAGERS.add((ServerManager) entry.getValue());
			}
		}
		log.info("[IoC] Collected {} ServerManagers in safe initialization order.", ORDERED_MANAGERS.size());
	}

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

	@Reflection(type = InitializeType.LISTENERS)
	public static void registerListeners(@NotNull ClassPath classPath) {
		try {
			for (var entry : BEAN_CONTAINER.entrySet()) {
				Class<?> clazz = entry.getKey();
				if (Listener.class.isAssignableFrom(clazz)) {
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
				if (CommandExecutor.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Registry.Command.class)) {
					Registry.Command autoCommand = clazz.getAnnotation(Registry.Command.class);

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
		taskReflection(true);
	}

	@Reflection(type = InitializeType.TASKS_OFF)
	public static void stopTasks(@Nullable ClassPath classPath) {
		taskReflection(false);
	}

	private static void taskReflection(boolean enable) {
		for (var entry : BEAN_CONTAINER.entrySet()) {
			Class<?> clazz = entry.getKey();
			if (!clazz.isAnnotationPresent(Task.class)) continue;
			if (!Tasks.class.isAssignableFrom(clazz)) continue;

			Tasks instance = (Tasks) entry.getValue();
			try {
				if (enable) {
					instance.startTask(plugin());
					log.info("Task {} started successfully.", clazz.getSimpleName());
				} else {
					instance.stopTask();
					log.info("Task {} stopped successfully.", clazz.getSimpleName());
				}
			} catch (Exception e) {
				log.error("Error occurred while processing task {}", clazz.getSimpleName(), e);
			}
		}
	}
}