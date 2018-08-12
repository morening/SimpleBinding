package com.morening.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by morening on 2018/7/17.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
        targetType = "android.view.View",
        setter = "setOnClickListener",
        type = "android.view.View.OnClickListener",
        methods = @ListenerMethod(
                name = "onClick",
                parameters = {"android.view.View"}
        )
)
public @interface OnClick {

    int[] id() default {};
}
