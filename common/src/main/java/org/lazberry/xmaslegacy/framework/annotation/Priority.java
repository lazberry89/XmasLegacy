package org.lazberry.xmaslegacy.framework.annotation;

import org.lazberry.xmaslegacy.framework.InitiatePriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the execution priority for {@code init()} or {@code close()} methods
 * in classes implementing {@link org.lazberry.xmaslegacy.framework.Initiator}.
 *
 * <p>Execution order: {@code FAST -> AVERAGE -> SLOW -> DELAYED}
 * <ul>
 *   <li>{@link InitiatePriority#FAST}: Executes earliest</li>
 *   <li>{@link InitiatePriority#AVERAGE}: Default priority</li>
 *   <li>{@link InitiatePriority#SLOW}: Executes later</li>
 *   <li>{@link InitiatePriority#DELAYED}: Executes latest</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    InitiatePriority value() default InitiatePriority.AVERAGE;
}
