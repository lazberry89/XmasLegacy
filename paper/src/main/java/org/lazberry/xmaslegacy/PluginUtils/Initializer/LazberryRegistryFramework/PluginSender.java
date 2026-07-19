package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PluginSender {
    void send(@NotNull String content);
}
