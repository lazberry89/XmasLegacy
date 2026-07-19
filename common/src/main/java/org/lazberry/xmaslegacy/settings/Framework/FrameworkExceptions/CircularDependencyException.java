package org.lazberry.xmaslegacy.settings.Framework.FrameworkExceptions;

public class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String message) {
        super(message);
    }
}
