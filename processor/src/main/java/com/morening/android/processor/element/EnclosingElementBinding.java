package com.morening.android.processor.element;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Element;

/**
 * Created by morening on 2018/7/22.
 */
public class EnclosingElementBinding {

    public Element element = null;

    public Map<Class<? extends Annotation>, TypeElementBinding> typeElementBindingMap = new LinkedHashMap<>();
}
