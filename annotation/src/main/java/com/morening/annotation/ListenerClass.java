package com.morening.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by morening on 2018/7/26.
 */

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ListenerClass {

    String targetType();

    String setter();

    String remover() default "";

    String type();

    ListenerMethod[] methods() default {};
}
