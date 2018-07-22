package com.morening.android.processor;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by morening on 2018/7/22.
 */
class TypeElementBinding {

    Class<?> typeElement = null;

    List<BindingElement> bindingElementList = new LinkedList<>();
}
