package org.lazberry.xmaslegacy.settings.Annotation;

import org.lazberry.xmaslegacy.settings.ConditionalRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conditional {
    Class<? extends ConditionalRegistry> value();
}
