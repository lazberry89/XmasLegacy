package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.InitializeType;

@Slf4j
public final class LazberryRegistryFramework {
    private static final @NotNull String VERSION = "LRF_26.7.18";
    private static @NotNull String packageName = "org.lazberry.xmaslegacy";
    private static boolean debugMode = true;

    public static boolean isDebug() {
        return debugMode;
    }

    public static void setDebug(boolean debug) {
        debugMode = debug;
    }

    /**
     * Target package name that reflections start to scan.
     * @return root package(String)
     * @see Package
     * @see String
     */
    @Contract(value = "-> !null", pure = true)
    public static @NotNull String rootPackage() {
        return packageName;
    }

    public static void setScanPackage(@NotNull String value) {
        packageName = value;
    }

    @Contract(pure = true)
    public static @NotNull String version() {
        return VERSION;
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static @NotNull <T> T icon(boolean component) {
        if (component) return (T) ColorUtils.chat("&#F45454[&#F7563FL&#FA592AR&#FC5B15F&#FF5D00]");
        return (T) ColorUtils.chatStr("&#F45454[&#F7563FL&#FA592AR&#FC5B15F&#FF5D00]");
    }

    private static void setup(@NotNull JavaPlugin ignored, @NotNull Class<? extends JavaPlugin> mainClass, boolean on) {
        String icon = icon(false);
        long startTime = System.currentTimeMillis();
        log.info("{} Booting LazberryRegistryFramework...", icon);

        try {
            ClassPath classPath =
                    ClassPath.from(mainClass.getClassLoader());
            if (on) Reflections.invokeReflections(classPath, InitializeType.TASKS_OFF);
            else Reflections.stopTasks(null);

            long current = System.currentTimeMillis() - startTime;
            if (on) log.info("{} Framework booted successfully in {}ms.", icon, current);
            else log.info("{} Framework cleanedUp successfully in {}ms.", icon, current);
        } catch (Exception e) {
            if (on) log.error("{} Framework failed to boot!", icon, e);
            else log.error("{} Framework failed to cleanup..", icon, e);
        }
    }

    public static void boot(@NotNull JavaPlugin plugin, @NotNull Class<? extends JavaPlugin> mainClass) {
        setScanPackage(mainClass.getPackageName());
        setup(plugin, mainClass, true);
    }

    public static void cleanUp(@NotNull JavaPlugin plugin, @NotNull Class<? extends JavaPlugin> mainClass) {
        setup(plugin, mainClass, false);
    }

    public static void registerExternalBean(@NotNull Class<?> clazz, @NotNull Object instance) {
        PackageScanner.registerInstance(clazz, instance);
    }
}
