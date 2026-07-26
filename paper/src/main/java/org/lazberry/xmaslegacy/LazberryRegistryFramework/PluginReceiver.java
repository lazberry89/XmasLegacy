package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PluginReceiver {
    void receive(@NotNull String content);
}
