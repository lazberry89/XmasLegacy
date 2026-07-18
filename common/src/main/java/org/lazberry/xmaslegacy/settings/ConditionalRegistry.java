package org.lazberry.xmaslegacy.settings;

@FunctionalInterface
public interface ConditionalRegistry {
    boolean matches();
}
