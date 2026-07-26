package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.SelfDestruct;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public final class DestructiveClassEngine {
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);
    private static final List<Object> TRANSIENT_COMPONENTS = Collections.synchronizedList(new ArrayList<>());

    public static void registerDestruction(@NotNull Class<?> clazz, @NotNull Object instance, @NotNull XmasLegacy plugin) {
        if (!clazz.isAnnotationPresent(SelfDestruct.class)) return;

        long delayTicks = clazz.getAnnotation(SelfDestruct.class).value();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                if (!DependencyContainer.getContainer().containsKey(clazz)) return;

                executeDestructionSequence(instance);
                DependencyContainer.getContainer().remove(clazz);

                if (LazberryRegistryFramework.isDebug()) log.info("{} [SelfDestruct-Singleton] Evicted: {}", icon, clazz.getSimpleName());
            } catch (Exception e) {
                log.error("{} [SelfDestruct] Error during destruction of {}", icon, clazz.getSimpleName(), e);
            }
        }, delayTicks);
    }

    public static void registerTransient(@NotNull Object instance, @NotNull XmasLegacy plugin) {
        Class<?> clazz = instance.getClass();
        if (!clazz.isAnnotationPresent(SelfDestruct.class)) {
            throw new IllegalArgumentException("[LRF-Strict] Transient component must have @SelfDestruct!");
        }

        long delayTicks = clazz.getAnnotation(SelfDestruct.class).value();

        TRANSIENT_COMPONENTS.add(instance);

        if (instance instanceof Listener) Bukkit.getPluginManager().registerEvents((Listener) instance, plugin);
        if (instance instanceof Tasks) ((Tasks) instance).startTask(plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!TRANSIENT_COMPONENTS.contains(instance)) return;

            try {
                executeDestructionSequence(instance);
                TRANSIENT_COMPONENTS.remove(instance); // 컬렉션에서 탈출

                log.info("{} [SelfDestruct-Transient] Safe exploded: {}", icon, clazz.getSimpleName());
            } catch (Exception e) {
                log.error("{} [SelfDestruct] Error during transient destruction", icon, e);
            }
        }, delayTicks);
    }

    public static void abortAllTransient() {
        log.info("{} [SelfDestruct] 셧다운 감지. 잔존하는 모든 임시 컴포넌트를 강제 소멸시킵니다...", icon);
        synchronized (TRANSIENT_COMPONENTS) {
            for (Object instance : TRANSIENT_COMPONENTS) {
                try {
                    executeDestructionSequence(instance);
                } catch (Exception e) {
                    log.error("{} Failed to abort component: {}", icon, instance.getClass().getSimpleName(), e);
                }
            }
            TRANSIENT_COMPONENTS.clear();
        }
    }

    private static void executeDestructionSequence(Object instance) {
        if (instance instanceof Destructible) ((Destructible) instance).onDestroy();
        if (instance instanceof Tasks) ((Tasks) instance).stopTask();
        if (instance instanceof Listener) HandlerList.unregisterAll((Listener) instance);
    }
}

