package com.morening.android.simplebinding;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by morening on 2018/7/19.
 */
public class SimpleBinding {

    public static Unbinder bind(Activity target){

        View sourceView = target.getWindow().getDecorView();
        String packageName = target.getPackageName();
        String className = target.getClass().getSimpleName();
        String targetClassName = packageName+"."+className+"_SimpleBinding";
        try {
            Class targetClass = Class.forName(targetClassName);
            Constructor constructor = targetClass.getConstructor(new Class[]{target.getClass(), View.class});
            return (Unbinder) constructor.newInstance(target, sourceView);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
