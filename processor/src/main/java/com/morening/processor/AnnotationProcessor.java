package com.morening.processor;

import com.morening.annotation.BindString;
import com.morening.annotation.BindView;
import com.morening.annotation.ListenerClass;
import com.morening.annotation.ListenerMethod;
import com.morening.annotation.OnClick;
import com.morening.processor.element.BindingElement;
import com.morening.processor.element.EnclosingElementBinding;
import com.morening.processor.element.TypeElementBinding;
import com.morening.processor.exception.IllegalArgumentsException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor{

    private static final ClassName VIEW =  ClassName.get("android.view", "View");
    private static final ClassName UNBINDER = ClassName.get("com.morening.android.simplebinding", "Unbinder");

    private Messager mMessager = null;
    private Filer mFiler = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(OnClick.class.getCanonicalName());
        types.add(BindView.class.getCanonicalName());
        types.add(BindString.class.getCanonicalName());

        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Map<String, EnclosingElementBinding> enclosingElementBindingMap = new LinkedHashMap<>();

        try {
            collectAndParseAllAnnotations(enclosingElementBindingMap, roundEnvironment);
            processAllAnnotations(enclosingElementBindingMap);

            return true;
        } catch (IllegalArgumentsException e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return false;
    }

    private void processAllAnnotations(Map<String, EnclosingElementBinding> enclosingElementBindingMap) {

        for (Map.Entry enclosingEntry: enclosingElementBindingMap.entrySet()){
            String enclosingElementKey = (String) enclosingEntry.getKey();
            EnclosingElementBinding enclosingElementBinding = (EnclosingElementBinding) enclosingEntry.getValue();

            String packageName = getPackageName(enclosingElementKey);
            String targetName = getSimpleName(enclosingElementKey);
            ClassName targetClassName = ClassName.get(packageName, targetName);

            TypeSpec.Builder clazzSpecBuilder = TypeSpec.classBuilder(targetName+"_SimpleBinding")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(UNBINDER);

            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addParameter(ParameterSpec.builder(targetClassName, "target", Modifier.FINAL).build())
                    .addParameter(ParameterSpec.builder(VIEW, "source").build())
                    .addModifiers(Modifier.PUBLIC);

            Map<Class<? extends Annotation>, TypeElementBinding> typeElementBindingMap =
                    enclosingElementBinding.typeElementBindingMap;
            for (Map.Entry typeEntry: typeElementBindingMap.entrySet()){
                Class<? extends Annotation> typeEntryKey = (Class<? extends Annotation>) typeEntry.getKey();
                TypeElementBinding typeElementBinding = (TypeElementBinding) typeEntry.getValue();

                List<BindingElement> bindingElementList = typeElementBinding.bindingElementList;
                for (BindingElement bindingElement: bindingElementList){
                    Element element = bindingElement.element;
                    String objectName = bindingElement.objectName;

                    if (typeEntryKey == OnClick.class){
                        if (!(element instanceof ExecutableElement) || element.getKind() != ElementKind.METHOD) {
                            throw new IllegalStateException("@OnClick annotation must be on a method.");
                        }

                        ListenerClass listener = typeEntryKey.getAnnotation(ListenerClass.class);
                        String setter = listener.setter();
                        String type = listener.type();
                        ListenerMethod[] methods = listener.methods();

                        int[] values = element.getAnnotation(OnClick.class).id();
                        for (int value: values){
                            TypeSpec.Builder listenerBuilder = TypeSpec.anonymousClassBuilder("")
                                    .addSuperinterface(convert2ClassName(type));
                            for (ListenerMethod method: methods){
                                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.name())
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC);
                                for (String parameter: method.parameters()){
                                    methodBuilder.addParameter(convert2ClassName(parameter), "view");
                                }
                                ExecutableElement executableElement = (ExecutableElement) element;
                                List<? extends VariableElement> variableElements = executableElement.getParameters();
                                if (variableElements.size() == 0){
                                    methodBuilder.addStatement("target.$L()", objectName);
                                } else {
                                    StringBuilder variables = new StringBuilder();
                                    for (VariableElement variableElement: variableElements){
                                        String variableElementName =
                                                variableElement.getSimpleName().toString();
                                        if (VIEW.simpleName().equalsIgnoreCase(variableElementName)){
                                            variables.append("view");
                                        } else {
                                            variables.append("null");
                                        }
                                        variables.append(",");
                                    }
                                    methodBuilder.addStatement("target.$L($L)", objectName, variables.toString().substring(0, variables.toString().length()-1));
                                }

                                listenerBuilder.addMethod(methodBuilder.build());
                            }

                            constructorBuilder.addStatement("source.findViewById($L).$L($L)",
                                    value,
                                    setter,
                                    listenerBuilder.build());
                        }
                    } else if (typeEntryKey == BindView.class){
                        int[] values = element.getAnnotation(BindView.class).id();
                        for (int value: values){
                            constructorBuilder.addStatement("target.$L = source.findViewById($L)",
                                    objectName,
                                    value);
                        }
                    } else if (typeEntryKey == BindString.class){
                        String value = element.getAnnotation(BindString.class).value();
                        int resId = element.getAnnotation(BindString.class).resId();
                        if (!"".equals(value)){
                            constructorBuilder.addStatement("target.$L = $S",
                                    objectName,
                                    value);
                        } else if (resId != 0){
                            constructorBuilder.addStatement("target.$L = target.getString($L)",
                                    objectName,
                                    resId);
                        }
                    }
                }
            }

            MethodSpec unbind = MethodSpec.methodBuilder("unbind")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addAnnotation(Override.class)
                    .build();

            clazzSpecBuilder.addMethod(constructorBuilder.build())
                    .addMethod(unbind)
                    .build();

            try {
                JavaFile javaFile = JavaFile.builder("com.morening.android.simplebinding", clazzSpecBuilder.build())
                        .addFileComment("This Java file was created by SimpleBingding.\nPlease don't edit it.")
                        .build();
                javaFile.writeTo(mFiler);
            } catch (IOException e) {

            }
        }
    }

    private void collectAndParseAllAnnotations(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) throws IllegalArgumentsException {

        collectAndParseOnClick(enclosingElementBindingMap, roundEnvironment);
        collectAndParseBindView(enclosingElementBindingMap, roundEnvironment);
        collectAndParseBindString(enclosingElementBindingMap, roundEnvironment);
    }

    private void collectAndParseBindString(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) throws IllegalArgumentsException {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindString.class)){
            String enclosingElementKey = element.getEnclosingElement().toString();

            EnclosingElementBinding enclosingElementBinding = enclosingElementBindingMap.get(enclosingElementKey);
            if (enclosingElementBinding == null){
                enclosingElementBinding = new EnclosingElementBinding();
                enclosingElementBindingMap.put(enclosingElementKey, enclosingElementBinding);
            }
            Map<Class<? extends Annotation>, TypeElementBinding> typeElementBindingMap = enclosingElementBinding.typeElementBindingMap;
            if (typeElementBindingMap == null){
                typeElementBindingMap = new LinkedHashMap<>();
                enclosingElementBinding.typeElementBindingMap = typeElementBindingMap;
            }
            TypeElementBinding typeElementBinding = typeElementBindingMap.get(BindString.class);
            if (typeElementBinding == null){
                typeElementBinding = new TypeElementBinding();
                typeElementBinding.typeElement = BindString.class;
                typeElementBindingMap.put(BindString.class, typeElementBinding);
            }
            List<BindingElement> bindElementList = typeElementBinding.bindingElementList;
            if (bindElementList == null){
                bindElementList = new ArrayList<>();
                typeElementBinding.bindingElementList = bindElementList;
            }
            BindingElement bindingElement = new BindingElement();
            bindingElement.element = element;
            bindingElement.objectName = element.getSimpleName().toString();

            String value = element.getAnnotation(BindString.class).value();
            int resId = element.getAnnotation(BindString.class).resId();
            if ("".equals(value) && resId == 0){
                throw new IllegalArgumentsException(
                        "Binding element should be attached with a String object!");
            }
            bindElementList.add(bindingElement);
        }
    }

    private void collectAndParseBindView(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) throws IllegalArgumentsException {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindView.class)){
            String enclosingElementKey = element.getEnclosingElement().toString();

            EnclosingElementBinding enclosingElementBinding = enclosingElementBindingMap.get(enclosingElementKey);
            if (enclosingElementBinding == null){
                enclosingElementBinding = new EnclosingElementBinding();
                enclosingElementBindingMap.put(enclosingElementKey, enclosingElementBinding);
            }
            Map<Class<? extends Annotation>, TypeElementBinding> typeElementBindingMap = enclosingElementBinding.typeElementBindingMap;
            if (typeElementBindingMap == null){
                typeElementBindingMap = new LinkedHashMap<>();
                enclosingElementBinding.typeElementBindingMap = typeElementBindingMap;
            }
            TypeElementBinding typeElementBinding = typeElementBindingMap.get(BindView.class);
            if (typeElementBinding == null){
                typeElementBinding = new TypeElementBinding();
                typeElementBinding.typeElement = BindView.class;
                typeElementBindingMap.put(BindView.class, typeElementBinding);
            }
            List<BindingElement> bindElementList = typeElementBinding.bindingElementList;
            if (bindElementList == null){
                bindElementList = new ArrayList<>();
                typeElementBinding.bindingElementList = bindElementList;
            }
            BindingElement bindingElement = new BindingElement();
            bindingElement.element = element;
            bindingElement.objectName = element.getSimpleName().toString();

            int[] ids = element.getAnnotation(BindView.class).id();
            if (ids.length == 0){
                throw new IllegalArgumentsException(
                        "Binding element should be attached with a view id at least!");
            }
            if (ids.length > 1){
                throw new IllegalArgumentsException(
                        "Binding element shouldn't be attached with more than one id!");
            }
            bindElementList.add(bindingElement);
        }
    }

    private void collectAndParseOnClick(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) throws IllegalArgumentsException {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(OnClick.class)){
            String enclosingElementKey = element.getEnclosingElement().toString();

            EnclosingElementBinding enclosingElementBinding = enclosingElementBindingMap.get(enclosingElementKey);
            if (enclosingElementBinding == null){
                enclosingElementBinding = new EnclosingElementBinding();
                enclosingElementBindingMap.put(enclosingElementKey, enclosingElementBinding);
            }
            Map<Class<? extends Annotation>, TypeElementBinding> typeElementBindingMap =
                    enclosingElementBinding.typeElementBindingMap;
            if (typeElementBindingMap == null){
                typeElementBindingMap = new LinkedHashMap<>();
                enclosingElementBinding.typeElementBindingMap = typeElementBindingMap;
            }
            TypeElementBinding typeElementBinding = typeElementBindingMap.get(OnClick.class);
            if (typeElementBinding == null){
                typeElementBinding = new TypeElementBinding();
                typeElementBinding.typeElement = OnClick.class;
                typeElementBindingMap.put(OnClick.class, typeElementBinding);
            }
            List<BindingElement> bindElementList = typeElementBinding.bindingElementList;
            if (bindElementList == null){
                bindElementList = new ArrayList<>();
                typeElementBinding.bindingElementList = bindElementList;
            }
            BindingElement bindingElement = new BindingElement();
            bindingElement.element = element;
            bindingElement.objectName = element.getSimpleName().toString();

            int[] ids = element.getAnnotation(OnClick.class).id();
            if (ids.length == 0){
                throw new IllegalArgumentsException(
                        "Binding element should be attached with a view id at least!");
            }
            bindElementList.add(bindingElement);
        }
    }

    private String getPackageName(String className){
        int lastIndex = className.lastIndexOf(".");
        if (lastIndex == -1){
            return className;
        }

        return className.substring(0, lastIndex);
    }

    private String getSimpleName(String className){
        int lastIndex = className.lastIndexOf(".");
        if (lastIndex == -1){
            return className;
        }

        return className.substring(lastIndex+1);
    }


    private ClassName convert2ClassName(String className){
        return ClassName.get(getPackageName(className), getSimpleName(className));
    }
}
