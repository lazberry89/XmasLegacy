package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.*;
import org.lazberry.xmaslegacy.Utils.ServerUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.ConditionalRegistry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private Class that Should be Used in only Reflections package. This class scans
 * all valid classes with {@link com.google.common.reflect.ClassPath} package utils.
 * Getter for all gathered container
 * is only privately opened to package. Never use scanning class in other distinctive class.
 * This class function is quite heavy. Only works when server starts-reflections work.
 * @see Reflections
 * @see ClassInitiator
 * @see Registry
 */
@Slf4j
final class PackageScanner {
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

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
                    if (LazberryRegistryFramework.isDebug()) log.debug("{} Skipping @ConsumableClass: {}", icon, clazz.getSimpleName());
                    continue;
                }

                if (ServerUtils.unCompatibleWithCurrentServer(clazz)) {
                    if (LazberryRegistryFramework.isDebug()) log.debug("{} Skipping incompatible class: {}", icon, clazz.getSimpleName());
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
                        clazz.isAnnotationPresent(Registry.class) ||
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
                                if (LazberryRegistryFramework.isDebug()) log.info("{} Skipping disabled component: {}", icon, clazz.getSimpleName());
                                continue;
                            }
                        } catch (Exception e) {
                            log.error("{} Failed to evaluate condition for {}", icon, clazz.getSimpleName(), e);
                            continue;
                        }
                    }

                    targetClasses.add(clazz);
                }
            } catch (Exception e) {
                if (LazberryRegistryFramework.isDebug()) log.warn("{} Failed to load class for scanning: {}", icon, classInfo.getName());
            }
        }

        for (Class<?> targetClass : targetClasses) {
            try {
                DependencyContainer.getOrCreateBean(targetClass);
            } catch (Exception e) {
                StructuralLog.logAssemblyFailure(DependencyContainer.constructionStack(), targetClass, e);
                if (LazberryRegistryFramework.isDebug()) log.error("{} Error StackTrace:", icon, e);
            }
        }
    }

    /**
     * Exception utility used only during testing or fallback scenarios. Resolves abstract interface
     * by matching concrete implementations marked with framework-level core annotations.
     * @param interfaceType Abstract target type to find implementation.
     * @return Matched concrete class, or null if none complies.
     */
    @TestOnly
    @Deprecated(since = "1.21.11", forRemoval = true)
    private static @Nullable Class<?> findImplementation(Class<?> interfaceType) {
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
                            Initiator.class.isAssignableFrom(candidate)) {
                        return candidate;
                    }
                }
            }
        } catch (Exception e) {
			log.error("{} Error occurred while finding implementation for {}", icon, interfaceType.getSimpleName(), e);
        }
        return null;
    }
}