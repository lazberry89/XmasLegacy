package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.FrameworkExceptions;

public class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String message) {
        super(message);
    }
}
