package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.ConfigValue;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions.CircularDependencyException;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions.NotCompatibleWithServerException;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions.NotValidInitializeTimingException;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions.VirtualClassInjectException;
import org.lazberry.xmaslegacy.Utils.ServerUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <h2>Core IoC / DI Assembly Matrix (Dependency Container Engine)</h2>
 * <p>
 * This class serves as the ultimate runtime heart and definitive single source of truth
 * for the Lazberry Registry Framework (LRF). It controls the central IoC (Inversion of Control)
 * context, orchestrating the entire lifecycle of managed singleton beans.
 * </p>
 * * <b>[Core Mechanics & Architectural Philosophy]:</b>
 * <ul>
 * <li><b>Recursive Topological Graph Resolution:</b> It parses constructor metadata dynamically via reflection
 * and resolves complex sub-dependency chains down to the leaf node seamlessly.</li>
 * <li><b>Strict Cycle Guardrails:</b> Utilizes a precise internal stack trace logic to intercept, isolate,
 * and instantly terminate fatal circular dependency graphs before they corrupt the runtime environment.</li>
 * <li><b>Immutable Context Architecture:</b> Effectively decouples the raw class-scanning phase from the
 * stateful instantiation layer, ensuring complete single-responsibility boundary isolation.</li>
 * </ul>
 * * <p>
 * All managed objects are securely mapped and preserved inside a deterministic order-retaining structure,
 * acting as an enterprise-grade bean registry for multi-platform network environments.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see PackageScanner
 * @see org.lazberry.xmaslegacy.Utils.ServerUtils
 * @see LrfInitializer
 */
@Slf4j
public final class DependencyContainer {
    private static final @NotNull Map<Class<?>, Object> BEAN_CONTAINER = new LinkedHashMap<>(30);
    private static final @NotNull Set<Class<?>> CONSTRUCTION_STACK = new LinkedHashSet<>(30);
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

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
     * Explicitly registers external instances (like main Plugin class context) into the container
     * prior to the classpath scanning phase.
     * @param clazz Target type key.
     * @param instance Actual instantiation data value.
     */
    public static void registerInstance(@NotNull Class<?> clazz, @NotNull Object instance) {
        BEAN_CONTAINER.put(clazz, instance);
        if (LazberryRegistryFramework.isDebug()) log.info("{} Pre-registered external bean: {}", icon, clazz.getSimpleName());
    }

    /**
     * Stack structure container to track circular dependencies during recursive bean instantiation.
     * Only opened package-private for checking container health or exception tracking.
     * @return Current construction stack tracking classes.
     * @see Set
     */
    static @NotNull Set<Class<?>> constructionStack() {
        return new LinkedHashSet<>(CONSTRUCTION_STACK);
    }

    /**
     * Core dependency injector engine. Recursively resolves constructor parameters,
     * tracks loop reference with constructionStack, and maps fully-instantiated instances into BEAN_CONTAINER.
     * @param clazz Target concrete class type to construct.
     * @return Fully managed singleton instance, or null if instantiation fails.
     * @throws Exception If constructor mapping or reflection execution fails.
     */
    static @Nullable Object getOrCreateBean(@NotNull Class<?> clazz) throws Exception {
        for (var entry : BEAN_CONTAINER.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new VirtualClassInjectException(
                    "[LRF-Strict] Cannot inject interface or abstract class directly: " + clazz.getName() +
                            ". Please inject a concrete implementation or register it manually via registerExternalBean."
            );
        }

        if (ServerUtils.unCompatibleWithCurrentServer(clazz))
            throw new NotCompatibleWithServerException(clazz.getSimpleName() + " is NOT compatible with the current server type!");

        if (CONSTRUCTION_STACK.contains(clazz))
            throw new CircularDependencyException("Circular dependency detected involving: " + clazz.getSimpleName());

        int currentDepth = CONSTRUCTION_STACK.size();
        StructuralLog.logDependencyStart(currentDepth, clazz);

        CONSTRUCTION_STACK.add(clazz);
        Object instance = null;

        try {
            if (clazz.isEnum()) {
                Field instanceField = clazz.getDeclaredField("INSTANCE");
                instance = instanceField.get(null);
            } else {
                Constructor<?> targetConstructor = getConstructor(clazz);

                java.lang.reflect.Parameter[] parameters = targetConstructor.getParameters();
                Object[] paramInstances = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    java.lang.reflect.Parameter param = parameters[i];
                    Class<?> paramType = param.getType();

                    if (param.isAnnotationPresent(ConfigValue.class)) {
                        paramInstances[i] = ConfigInjection.resolve(
                                param.getAnnotation(ConfigValue.class),
                                paramType,
                                clazz
                        );
                        continue;
                    }
                    paramInstances[i] = getOrCreateBean(paramType);
                }

                instance = targetConstructor.newInstance(paramInstances);
                StructuralLog.logAssemblySuccess(currentDepth, clazz, parameters.length);

                if (instance instanceof LrfInitializer) {
                    try {
                        ((LrfInitializer) instance).afterPropertiesSet();

                        StructuralLog.logLifecycleSuccess(currentDepth, clazz);
                    } catch (NotValidInitializeTimingException e) {
                        log.error("{} Initialization timing error for: {}", icon, clazz.getSimpleName(), e);
                        throw e;
                    }
                }
            }

            if (instance != null) BEAN_CONTAINER.put(clazz, instance);
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
}
