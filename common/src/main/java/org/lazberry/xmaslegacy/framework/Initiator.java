package org.lazberry.xmaslegacy.framework;

public interface Initiator {
    void init();
    default void close() {}
}
