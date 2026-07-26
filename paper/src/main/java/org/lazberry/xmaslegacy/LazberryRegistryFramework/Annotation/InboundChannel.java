package org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InboundChannel("bungeecord:main")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InboundChannel {
    @NotNull String value();
}
