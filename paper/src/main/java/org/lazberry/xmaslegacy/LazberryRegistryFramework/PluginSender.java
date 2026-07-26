package org.lazberry.xmaslegacy.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PluginSender {
    void send(@NotNull String content);
}
