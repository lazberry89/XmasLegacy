package org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation;

import org.lazberry.xmaslegacy.settings.PlayerSkills;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Skill {
    PlayerSkills type();
}
