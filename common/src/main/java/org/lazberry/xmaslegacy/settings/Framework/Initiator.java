package org.lazberry.xmaslegacy.settings.Framework;

public interface Initiator {
    void init();
    default void close() {}
}
