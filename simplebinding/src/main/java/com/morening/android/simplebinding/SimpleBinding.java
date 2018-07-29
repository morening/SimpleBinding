package com.morening.android.simplebinding;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by morening on 2018/7/19.
 */
@SuppressWarnings("WeakerAccess")
public class SimpleBinding {

    public static Unbinder bind(Activity target){

        View sourceView = target.getWindow().getDecorView();
        String packageName = target.getPackageName();
        String className = target.getClass().getSimpleName();
        String targetClassName = packageName+"."+className+"_SimpleBinding";
        try {
            Class targetClass = Class.forName(targetClassName);
            Constructor constructor = targetClass.getConstructor(target.getClass(), View.class);
            return (Unbinder) constructor.newInstance(target, sourceView);
        } catch (InstantiationException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
