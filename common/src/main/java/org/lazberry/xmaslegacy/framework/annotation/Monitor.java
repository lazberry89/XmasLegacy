package org.lazberry.xmaslegacy.framework.annotation;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.framework.Observation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitor {
	@NotNull Observation lvl() default Observation.LOW;
}
