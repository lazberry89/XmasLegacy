package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutboundChannel {
    @NotNull String[] value() default {"bungeecord::main"};
}
