package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.InitializeType;

/**
 * <h2>LazberryRegistryFramework (LRF Bootstrapper & Global Context Manager)</h2>
 * <p>
 * The central bootstrapper and core configuration matrix of the Lazberry Registry Framework (LRF).
 * This class orchestrates the initialization sequence, bridges classpath data, controls global debugging states,
 * and exposed structural visualization configurations.
 * </p>
 * * <b>[Operational Mechanics & Core Modules]:</b>
 * <ul>
 * <li><b>Dynamic Package Target Matching:</b> Automatically intercepts the main plugin's runtime package
 * during the boot phase, programmatically establishing the boundary for reflection scans.</li>
 * <li><b>Stateful Bootstrapping Chain:</b> Loads classloader metadata via Guava's {@link ClassPath},
 * triggers reflective scanning through {@link Reflections}, and initializes the recursive dependency matrix.</li>
 * <li><b>External Component Bridge:</b> Provides a secure portal to register unmanaged external instances
 * (such as native Bukkit wrappers or the main plugin instance) directly into the {@link DependencyContainer}.</li>
 * </ul>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see PackageScanner
 * @see DependencyContainer
 * @see Reflections
 */
@Slf4j
@SuppressWarnings("unchecked")
public final class LazberryRegistryFramework {
    private static final @NotNull String VERSION = "LRF_26.7.19";
    private static final @NotNull String success = "&a[SUCCESS]";
    private static final @NotNull String failure = "&c[FAILURE]";
    private static @NotNull String packageName = "org.lazberry.xmaslegacy";
    private static @NotNull @Getter @Setter String defaultChannel = "";
    private static boolean debugMode = true;
    private static boolean drawStructure = true;

    public static void drawStructureLog(boolean flag) {
        drawStructure = flag;
    }

    public static boolean isLogDrawStructure() {
        return drawStructure;
    }

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
    public static @NotNull <T> T icon(boolean component) {
        if (component) return (T) ColorUtils.chat("&#F45454[&#F7563FL&#FA592AR&#FC5B15F&#FF5D00]");
        return (T) "§6[LRF]";
    }

    @Contract(pure = true)
    public static @NotNull <T> T IoC(boolean component) {
        if (component) return (T) ColorUtils.chat("&#001CFF[&#0045FFI&#006FFFo&#0098FFC&#00C1FF]");
        return (T) ColorUtils.chatStr("&#001CFF[&#0045FFI&#006FFFo&#0098FFC&#00C1FF]");
    }

    static @NotNull String successIcon() {
        return success;
    }

    static @NotNull String failureIcon() {
        return failure;
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
        DependencyContainer.registerInstance(clazz, instance);
    }
}
