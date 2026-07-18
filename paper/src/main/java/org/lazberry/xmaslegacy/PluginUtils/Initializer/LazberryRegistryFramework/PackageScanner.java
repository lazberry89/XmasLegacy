package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.Annotation.*;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions.NotValidInitializeTimingException;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.ServerInitializer;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ConditionalRegistry;
import org.lazberry.xmaslegacy.settings.ServerManager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.lang.reflect.*;
import java.util.*;

/**
 * Package-private Class that Should be Used in only Reflections package. This class scans
 * all valid classes with {@link com.google.common.reflect.ClassPath} package utils.
 * Getter for all gathered container
 * is only privately opened to package. Never use scanning class in other distinctive class.
 * This class function is quite heavy. Only works when server starts-reflections work.
 * @see Reflections
 * @see ManagerInjection
 * @see Registry
 */
@Slf4j
final class PackageScanner {
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);
    private static final @NotNull Map<Class<?>, Object> BEAN_CONTAINER = new LinkedHashMap<>();
    private static final @NotNull Set<Class<?>> CONSTRUCTION_STACK = new HashSet<>();

    /**
     * Lazy-Initialize for plugin. This Scanner methods are all static,
     * because if initialize timing the plugin instance can be null, so
     * using only when plugin instance is needed, preventing {@link NullPointerException}
     * @return plugin instance
     * @see XmasLegacy
     */
    @Contract(value = "-> !null", pure = true)
    private static @NotNull XmasLegacy plugin() {
        return XmasLegacy.getInstance();
    }

    /**
     * Not Shallow-Copy. Memory address, inner values are all same. In any way,
     * never use in another class, package. Only in {@link Reflections} and {@link ManagerInjection}
     * for Ioc engine start.
     * @return Mapped container that mapped Class with actual generated class instance.
     * @see Map
     * @see Class
     */
    @Contract(pure = true)
    static @NotNull Map<Class<?>, Object> getContainer() {
        return BEAN_CONTAINER;
    }

    /**
     * Directly retrieves a fully managed bean instance from the container by its type.
     * Automatically handles casting so you don't have to write messy explicit casting code.
     * Returns null if no matching bean or assignable instance is found.
     *
     * @param clazz Target class type of the bean to retrieve.
     * @param <T> The generic type of the bean.
     * @return Fully cast managed bean instance, or null if missing.
     * @see Map
     */
    @SuppressWarnings("unchecked")
    public static @Nullable <T> T getBean(@NotNull Class<T> clazz) {
        for (var entry : BEAN_CONTAINER.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }
        return null;
    }

    /**
     * Stack structure container to track circular dependencies during recursive bean instantiation.
     * Only opened package-private for checking container health or exception tracking.
     * @return Current construction stack tracking classes.
     * @see Set
     */
    static @NotNull Set<Class<?>> constructionStack() {
        return new HashSet<>(CONSTRUCTION_STACK);
    }

    /**
     * 💡 서버 호환성 검증의 궁극적 단일화
     * Task, Command 등 그 어떤 컴포넌트라도 오직 Include/Exclude 규칙만 따릅니다.
     * 둘 다 없다면 기본적으로 GLOBAL(전체 허용)으로 간주합니다.
     * @param clazz Target class to verify compatibility.
     * @return True if UnCompatible with current server runtime, false otherwise.
     */
    private static boolean unCompatibleWithCurrentServer(@NotNull Class<?> clazz) {
        ServerType current = ServerInitializer.getServerType(plugin());

        if (clazz.isAnnotationPresent(Registry.Exclude.class)) {
            Registry.Exclude exclude = clazz.getAnnotation(Registry.Exclude.class);
            for (ServerType excludedType : exclude.type()) {
                if (excludedType == current) return true;
            }
        }

        if (clazz.isAnnotationPresent(Registry.Include.class)) {
            Registry.Include include = clazz.getAnnotation(Registry.Include.class);
            boolean matched = false;
            for (ServerType targetType : include.type()) {
                if (!isServerTypeUnCompatible(targetType, current)) {
                    matched = true;
                    break;
                }
            }
            return !matched;
        }
        return false;
    }

    /**
     * Checks if both {@link ServerType} is valid. when {@link ServerType#GLOBAL}, it contains all
     * serverType.
     * @param targetType server type from collected instance.
     * @param currentType current {@link org.bukkit.Server}'s ServerType.
     * @return if UnCompatible, returns true, else false.
     * @see ServerType
     */
    @Contract(pure = true)
    private static boolean isServerTypeUnCompatible(@NotNull ServerType targetType, @NotNull ServerType currentType) {
        if (targetType == currentType) return false;
        return targetType != ServerType.GLOBAL || !currentType.isRequiresGlobalInitializer();
    }

    /**
     * Core scan loop that gathers target classes based on specific annotations, filters out
     * interface/abstract/incompatible types, and triggers the bean container instantiation flow.
     * @param classPath Guava's ClassPath utils containing current classloader's metadata.
     * @see ClassPath
     */
    static void buildAndInjectBeans(@NotNull ClassPath classPath) {
        log.info("{} Building Bean Container and resolving constructor dependencies...", icon);

        List<Class<?>> targetClasses = new ArrayList<>();
        for (var classInfo : classPath.getTopLevelClassesRecursive(LazberryRegistryFramework.rootPackage())) {
            try {
                Class<?> clazz = classInfo.load();
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                if (clazz.isAnnotationPresent(org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass.class)) {
                    log.debug("{} Skipping @ConsumableClass: {}", icon, clazz.getSimpleName());
                    continue;
                }

                if (unCompatibleWithCurrentServer(clazz)) {
                    log.debug("{} Skipping incompatible class: {}", icon, clazz.getSimpleName());
                    continue;
                }

                boolean hasInjectConstructor = false;
                for (var constructor : clazz.getDeclaredConstructors()) {
                    if (constructor.isAnnotationPresent(Inject.class)) {
                        hasInjectConstructor = true;
                        break;
                    }
                }

                if (clazz.isAnnotationPresent(Registry.Include.class) ||
                        clazz.isAnnotationPresent(Registry.Exclude.class) ||
                        clazz.isAnnotationPresent(Commands.class) ||
                        clazz.isAnnotationPresent(Listeners.class) ||
                        clazz.isAnnotationPresent(Skill.class) ||
                        clazz.isAnnotationPresent(Roles.class) ||
                        clazz.isAnnotationPresent(Task.class) ||
                        hasInjectConstructor) {

                    if (clazz.isAnnotationPresent(org.lazberry.xmaslegacy.settings.Annotation.Conditional.class)) {
                        var conditionalAnno = clazz.getAnnotation(org.lazberry.xmaslegacy.settings.Annotation.Conditional.class);
                        try {
                            ConditionalRegistry condition = conditionalAnno.value().getDeclaredConstructor().newInstance();

                            if (!condition.matches()) {
                                log.info("{} [LRF-Conditional] Skipping disabled component: {}", icon, clazz.getSimpleName());
                                continue;
                            }
                        } catch (Exception e) {
                            log.error("{} [LRF-Conditional] Failed to evaluate condition for {}", icon, clazz.getSimpleName(), e);
                            continue;
                        }
                    }

                    targetClasses.add(clazz);
                }
            } catch (Exception e) {
                log.warn("{} Failed to load class for scanning: {}", icon, classInfo.getName());
            }
        }

        for (Class<?> targetClass : targetClasses) {
            try {
                getOrCreateBean(targetClass);
            } catch (Exception e) {
                log.error("{} CRITICAL | Failed to instantiate bean: {}", icon, targetClass.getSimpleName(), e);
            }
        }
    }

    /**
     * Core dependency injector engine. Recursively resolves constructor parameters,
     * tracks loop reference with constructionStack, and maps fully-instantiated instances into BEAN_CONTAINER.
     * @param clazz Target concrete class type to construct.
     * @return Fully managed singleton instance, or null if instantiation fails.
     * @throws Exception If constructor mapping or reflection execution fails.
     */
    private static @Nullable Object getOrCreateBean(@NotNull Class<?> clazz) throws Exception {
        for (var entry : BEAN_CONTAINER.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            Class<?> implementationClass = findImplementation(clazz);
            if (implementationClass == null) {
                throw new NoSuchElementException("Cannot find any registered implementation for: " + clazz.getName());
            }
            return getOrCreateBean(implementationClass);
        }

        if (unCompatibleWithCurrentServer(clazz)) {
            throw new IllegalStateException(clazz.getSimpleName() + " is NOT compatible with the current server type!");
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
                Constructor<?> targetConstructor = getConstructor(clazz);
                Class<?>[] paramTypes = targetConstructor.getParameterTypes();
                Object[] paramInstances = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    Class<?> paramType = paramTypes[i];
                    paramInstances[i] = getOrCreateBean(paramType);
                }

                instance = targetConstructor.newInstance(paramInstances);
                log.debug("{} Successfully created bean: {} with {} dependencies.", icon, clazz.getSimpleName(), paramTypes.length);

                if (instance instanceof LrfInitializer) {
                    try {
                        ((LrfInitializer) instance).afterPropertiesSet();
                        log.info("{} Callback executed for: {}", icon, clazz.getSimpleName());
                    } catch (NotValidInitializeTimingException e) {
                        log.error("{} Initialization timing error for: {}", icon, clazz.getSimpleName(), e);
                        throw e;
                    }
                }
            }

            if (instance != null) {
                BEAN_CONTAINER.put(clazz, instance);
            }

            return instance;

        } finally {
            CONSTRUCTION_STACK.remove(clazz);
        }
    }

    /**
     * Resolves appropriate constructor for DI. Prioritizes {@link Inject} annotated
     * constructor, defaults to no-arg constructor if not found.
     * @param clazz Target class to extract constructor.
     * @return Accessible constructor instance.
     * @throws NoSuchMethodException If default constructor is also missing.
     */
    private static @NotNull Constructor<?> getConstructor(@NotNull Class<?> clazz) throws NoSuchMethodException {
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
        return targetConstructor;
    }

    /**
     * Exception utility used only during testing or fallback scenarios. Resolves abstract interface
     * by matching concrete implementations marked with framework-level core annotations.
     * @param interfaceType Abstract target type to find implementation.
     * @return Matched concrete class, or null if none complies.
     */
    @TestOnly
    private static Class<?> findImplementation(Class<?> interfaceType) {
        try {
            ClassPath classPath = com.google.common.reflect.ClassPath.from(PackageScanner.class.getClassLoader());
            for (var classInfo : classPath.getTopLevelClassesRecursive(LazberryRegistryFramework.rootPackage())) {
                Class<?> candidate = classInfo.load();
                if (candidate.isInterface() || Modifier.isAbstract(candidate.getModifiers())) continue;
                if (candidate.isAnnotationPresent(org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass.class)) {
                    continue;
                }
                if (interfaceType.isAssignableFrom(candidate)) {
                    if (candidate.isAnnotationPresent(Registry.Include.class) ||
                            candidate.isAnnotationPresent(Registry.Exclude.class) ||
                            candidate.isAnnotationPresent(Commands.class) ||
                            ServerManager.class.isAssignableFrom(candidate)) {
                        return candidate;
                    }
                }
            }
        } catch (Exception e) {
            log.error("{} Error occurred while finding implementation for {}", icon, interfaceType.getSimpleName(), e);
        }
        return null;
    }

    /**
     * Explicitly registers external instances (like main Plugin class context) into the container
     * prior to the classpath scanning phase.
     * @param clazz Target type key.
     * @param instance Actual instantiation data value.
     */
    public static void registerInstance(@NotNull Class<?> clazz, @NotNull Object instance) {
        BEAN_CONTAINER.put(clazz, instance);
        log.info("{} Pre-registered external bean: {}", icon, clazz.getSimpleName());
    }
}