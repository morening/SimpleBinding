package com.morening.android.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by morening on 2018/7/26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ListenerMethod {

    String name();

    String[] parameters() default {};

    String returnType() default "void";

    String defaultReturn() default "null";
}
