package org.lazberry.xmaslegacy.framework.annotation;

import org.lazberry.xmaslegacy.framework.ConditionalRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conditional {
    Class<? extends ConditionalRegistry> value();
}
