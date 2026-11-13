package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import com.google.common.reflect.ClassPath;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.Velocity;
import org.lazberry.xmaslegacy.framework.InitializeType;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lazberry.xmaslegacy.LazberryRegistryFramework.ClassInitiator.collectManagers;
import static org.lazberry.xmaslegacy.LazberryRegistryFramework.ClassInitiator.initializeManagers;

/**
 * Public execution room for LazberryRegistryFramework lifecycle triggers.
 * This class takes fully initialized beans from {@link PackageScanner}, then automatically
 * binds them to Velocity EventManager, CommandManager, and Custom Registry systems.
 * All method invocations are dynamically mapped by {@link InitializeType}.
 *
 * @see PackageScanner
 * @see InitializeType
 * @see Reflection
 */
@Slf4j
public final class Reflections {
	private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

	/**
	 * Lazy-lookup wrapper for main plugin core instance.
	 * @return Current singleton instance of Velocity ProxyServer context.
	 */
	private static @NotNull ProxyServer plugin() {
		return Velocity.getInstance();
	}

	private static @NotNull Velocity pluginMainInstance() {
		return Velocity.getVelocityInstance();
	}

	/**
	 * Entry point for the whole LRF IoC boot process. Pre-registers core platform contexts,
	 * triggers dependency scanning, and dynamically invokes all internal @Reflection-marked
	 * registry methods unless explicitly blacklisted.
	 *
	 * @param classPath  Guava's analyzed classpath metadata.
	 * @param exceptions Specific initialize types to skip or ignore during this reflection loop.
	 */
	@Reflection(type = InitializeType.EXCEPTED)
	public static void invokeReflections(@NotNull ClassPath classPath, InitializeType... exceptions) {
		DependencyContainer.registerInstance(Velocity.class, pluginMainInstance());
		DependencyContainer.registerInstance(ProxyServer.class, plugin());
		DependencyContainer.registerInstance(File.class, Velocity.directory());

		PackageScanner.buildAndInjectBeans(classPath);
		collectManagers();
		initializeManagers();

		// gatherRegisteredDestruction(); // 필요시 활성화

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
				log.info("{} Register {} has been invoked", icon, method.getName());
			} catch (Exception e) {
				log.error("{} Error occurred while invoking reflection for method {}", icon, method.getName(), e);
			}
		}
	}

	/**
	 * Scans the container for classes containing Velocity event handlers (methods marked with @Subscribe)
	 * and automatically registers them into the Velocity EventManager.
	 *
	 * @param classPath Automatically provided classpath metadata (unused but kept for reflection invoke uniformity).
	 */
	@Reflection(type = InitializeType.LISTENERS)
	public static void registerListeners(@NotNull ClassPath classPath) {
		try {
			for (var entry : DependencyContainer.getContainer().entrySet()) {
				Class<?> clazz = entry.getKey();
				Object instance = entry.getValue();

				// 클래스 내부에 @Subscribe 어노테이션이 붙은 메서드가 하나라도 존재하면 이벤트 리스너로 간주
				boolean hasSubscribeMethod = false;
				for (Method method : clazz.getDeclaredMethods()) {
					if (method.isAnnotationPresent(Subscribe.class)) {
						hasSubscribeMethod = true;
						break;
					}
				}

				if (hasSubscribeMethod) {
					plugin().getEventManager().register(pluginMainInstance(), instance);
					log.info("{} Velocity Listener/Event handler {} Automatically registered from IoC Container", icon, clazz.getSimpleName());
				}
			}
		} catch (Exception e) {
			log.error("{} Error occurred while registering all Velocity listeners", icon, e);
		}
	}

	/**
	 * Scans the container for {@link SimpleCommand} implementations that are marked with the
	 * {@link Commands} annotation, and safely registers them into the Velocity CommandManager.
	 *
	 * @param classPath Automatically provided classpath metadata (unused but kept for reflection invoke uniformity).
	 */
	@Reflection(type = InitializeType.COMMANDS)
	public static void registerCommands(@NotNull ClassPath classPath) {
		try {
			var commandManager = plugin().getCommandManager();

			for (var entry : DependencyContainer.getContainer().entrySet()) {
				Class<?> clazz = entry.getKey();
				if (SimpleCommand.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Commands.class)) {
					Commands autoCommand = clazz.getAnnotation(Commands.class);

					List<String> allAliases = new ArrayList<>();
					allAliases.add(autoCommand.command());
					allAliases.addAll(Arrays.asList(autoCommand.aliases()));

					SimpleCommand commandInstance = (SimpleCommand) entry.getValue();

					String primaryCommand = allAliases.getFirst();
					String[] secondaryAliases = allAliases.subList(1, allAliases.size()).toArray(String[]::new);

					CommandMeta meta = commandManager.metaBuilder(primaryCommand)
							.aliases(secondaryAliases)
							.build();

					commandManager.register(meta, commandInstance);
					log.info("{} Command {} (with aliases) Automatically registered from IoC Container", icon, primaryCommand);
				}
			}
		} catch (Exception e) {
			log.error("{} Error occurred while registering all Velocity Commands", icon, e);
		}
	}
}