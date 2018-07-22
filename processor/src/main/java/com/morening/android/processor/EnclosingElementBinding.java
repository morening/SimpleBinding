package com.morening.android.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Element;

/**
 * Created by morening on 2018/7/22.
 */
class EnclosingElementBinding {

    Element element = null;

    Map<Class<?>, TypeElementBinding> typeElementBindingMap = new LinkedHashMap<>();
}
