package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PluginReceiver {
    void receive(@NotNull String content);
}
