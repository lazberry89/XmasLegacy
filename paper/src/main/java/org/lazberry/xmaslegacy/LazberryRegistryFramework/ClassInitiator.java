package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.framework.InitiatePriority;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.annotation.Priority;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Core Orchestrator for ServerManager lifecycle initialization in LazberryRegistryFramework.
 *
 * <p><h3>Architecture & Operational Principle</h3>
 * This class acts as a dedicated control bridge between the raw IoC storage ({@link DependencyContainer})
 * and the Bukkit platform boot logic.
 *
 * <p>Methods {@link Initiator#init()} and {@link Initiator#close()} can be annotated with {@link Priority}
 * to control their execution order:
 * <ul>
 * <li><b>Execution order:</b> {@code FAST -> AVERAGE -> SLOW -> DELAYED}</li>
 * </ul>
 */
@Slf4j
final class ClassInitiator {
    private static final @NotNull List<Initiator> MANAGERS = new ArrayList<>();
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

    /**
     * Phase 1: Context Filtering & Collection Routine.
     * Clears historical references and extracts singleton beans implementing {@link Initiator}.
     */
    static void collectManagers() {
        MANAGERS.clear();
        for (var entry : DependencyContainer.getContainer().entrySet()) {
            Class<?> clazz = entry.getKey();

            if (Initiator.class.isAssignableFrom(clazz)) {
                MANAGERS.add((Initiator) entry.getValue());
            }
        }
        log.info("{} Collected {} ServerManagers.", icon, MANAGERS.size());
    }

    /**
     * Phase 2: Sequential Lifecycle Invocation Engine.
     * Sorts managers by their {@code init()} method's {@link Priority}
     * ({@code FAST -> AVERAGE -> SLOW -> DELAYED}) and triggers {@code init()}.
     */
    static void initializeManagers() {
        log.info("[IoC] Initializing Initiators sequentially by init() priority...");
        List<Initiator> initList = new ArrayList<>(MANAGERS);
        initList.sort(Comparator.comparing(ClassInitiator::getInitPriority));

        for (Initiator manager : initList) {
            try {
                manager.init();
                log.info("{} Initialized Initiator: {} (Init Priority: {})",
                        icon, manager.getClass().getSimpleName(), getInitPriority(manager));
            } catch (Exception e) {
                log.error("{} Failed to initialize Initiator: {}",
                        icon, manager.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Phase 3: Ordered Lifecycle Shutdown Engine.
     * Sorts managers by their {@code close()} method's {@link Priority}
     * ({@code FAST -> AVERAGE -> SLOW -> DELAYED}) and triggers {@code close()}.
     */
    static void closeupManagers() {
        log.info("[IoC] Closing Initiators sequentially by close() priority...");
        List<Initiator> closeList = new ArrayList<>(MANAGERS);
        closeList.sort(Comparator.comparing(ClassInitiator::getClosePriority));

        for (Initiator manager : closeList) {
            try {
                manager.close();
                log.info("{} Closed Initiator: {} (Close Priority: {})",
                        icon, manager.getClass().getSimpleName(), getClosePriority(manager));
            } catch (Exception e) {
                log.error("{} Failed to Closeup Initiator: {}",
                        icon, manager.getClass().getSimpleName(), e);
            }
        }
        MANAGERS.clear();
    }

    private static @NotNull InitiatePriority getInitPriority(@NotNull Initiator initiator) {
        try {
            Method method = initiator.getClass().getMethod("init");
            Priority priority = method.getAnnotation(Priority.class);
            if (priority != null) {
                return priority.value();
            }
        } catch (NoSuchMethodException ignored) {}

        Priority classPriority = initiator.getClass().getAnnotation(Priority.class);
        return classPriority != null ? classPriority.value() : InitiatePriority.AVERAGE;
    }

    private static @NotNull InitiatePriority getClosePriority(@NotNull Initiator initiator) {
        try {
            Method method = initiator.getClass().getMethod("close");
            Priority priority = method.getAnnotation(Priority.class);
            if (priority != null) {
                return priority.value();
            }
        } catch (NoSuchMethodException ignored) {}

        Priority classPriority = initiator.getClass().getAnnotation(Priority.class);
        return classPriority != null ? classPriority.value() : InitiatePriority.AVERAGE;
    }
}
