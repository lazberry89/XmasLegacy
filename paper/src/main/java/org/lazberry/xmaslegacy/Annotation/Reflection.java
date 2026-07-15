package org.lazberry.xmaslegacy.Annotation;

import org.lazberry.xmaslegacy.PluginUtils.Initializer.InitializeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Reflection {
    InitializeType type();
}
