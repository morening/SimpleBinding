package com.morening.android.processor;

import com.morening.android.annotation.BindView;
import com.morening.android.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor{

    private static final ClassName VIEW =  ClassName.get("android.view", "View");
    private static final ClassName UNBINDER = ClassName.get("com.morening.android.simplebinding", "Unbinder");
    private static final ClassName ONCLICKLISTENER = ClassName.get("android.view.View", "OnClickListener");

    private Filer mFiler = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(OnClick.class.getCanonicalName());
        types.add(BindView.class.getCanonicalName());

        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Map<String, EnclosingElementBinding> enclosingElementBindingMap = new LinkedHashMap<>();

        collectAndParseAllAnnotations(enclosingElementBindingMap, roundEnvironment);
        processAllAnnotations(enclosingElementBindingMap);

        return true;
    }

    private void processAllAnnotations(Map<String, EnclosingElementBinding> enclosingElementBindingMap) {

        for (Map.Entry entry: enclosingElementBindingMap.entrySet()){
            EnclosingElementBinding enclosingElementBinding = (EnclosingElementBinding) entry.getValue();
            Element element = enclosingElementBinding.element;

            String packageName = getPackageName(element.getEnclosingElement());
            String targetName = element.getEnclosingElement().getSimpleName().toString();
            ClassName targetClassName = ClassName.get(packageName, targetName);

            TypeSpec.Builder clazzSpecBuilder = TypeSpec.classBuilder(targetName+"_SimpleBinding")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(UNBINDER);

            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addParameter(ParameterSpec.builder(targetClassName, "target", Modifier.FINAL).build())
                    .addParameter(ParameterSpec.builder(VIEW, "source").build())
                    .addModifiers(Modifier.PUBLIC);

            Map<Class<?>, TypeElementBinding> typeElementBindingMap = enclosingElementBinding.typeElementBindingMap;
            for (Map.Entry typeEntry: typeElementBindingMap.entrySet()){
                Class typeElement = (Class) typeEntry.getKey();
                TypeElementBinding typeElementBinding = (TypeElementBinding) typeEntry.getValue();

                List<BindingElement> bindingElementList = typeElementBinding.bindingElementList;
                for (BindingElement bindingElement: bindingElementList){
                    String objectName = bindingElement.objectName;
                    int value = bindingElement.value;

                    if (typeElement == OnClick.class){
                        TypeSpec onClickListener = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(ONCLICKLISTENER)
                                .addMethod(MethodSpec.methodBuilder("onClick")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(VIEW, "view")
                                        .addStatement("$N.$L(view)", "target", objectName)
                                        .build())
                                .build();

                        constructorBuilder.addStatement("$N.findViewById($L).setOnClickListener($L)",
                                "source",
                                value,
                                onClickListener);
                    } else if (typeElement == BindView.class){
                        constructorBuilder.addStatement("$N.$L = $N.findViewById($L)",
                                "target",
                                objectName,
                                "source",
                                value);
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
                JavaFile javaFile = JavaFile.builder("com.morening.android.simplebinding", clazzSpecBuilder.build()).build();
                javaFile.writeTo(mFiler);
            } catch (IOException e) {

            }
        }
    }

    private void collectAndParseAllAnnotations(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) {

        collectAndParseOnClick(enclosingElementBindingMap, roundEnvironment);
        collectAndParseBindView(enclosingElementBindingMap, roundEnvironment);
    }

    private void collectAndParseBindView(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(BindView.class)){
            String enclosingElementKey = element.getEnclosingElement().toString();
            EnclosingElementBinding enclosingElementBinding = enclosingElementBindingMap.get(enclosingElementKey);
            if (enclosingElementBinding == null){
                enclosingElementBinding = new EnclosingElementBinding();
                enclosingElementBinding.element = element;
                enclosingElementBindingMap.put(enclosingElementKey, enclosingElementBinding);
            }
            Map<Class<?>, TypeElementBinding> typeElementBindingMap = enclosingElementBinding.typeElementBindingMap;
            TypeElementBinding typeElementBinding = typeElementBindingMap.get(BindView.class);
            if (typeElementBinding == null){
                typeElementBinding = new TypeElementBinding();
                typeElementBinding.typeElement = BindView.class;
                typeElementBindingMap.put(BindView.class, typeElementBinding);
            }
            List<BindingElement> bindElementList = typeElementBinding.bindingElementList;
            BindingElement bindingElement = new BindingElement();
            bindingElement.objectName = element.getSimpleName().toString();
            bindingElement.value = element.getAnnotation(BindView.class).id();
            bindElementList.add(bindingElement);
        }
    }

    private void collectAndParseOnClick(
            Map<String, EnclosingElementBinding> enclosingElementBindingMap,
            RoundEnvironment roundEnvironment) {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(OnClick.class)){
            String enclosingElementKey = element.getEnclosingElement().toString();
            EnclosingElementBinding enclosingElementBinding = enclosingElementBindingMap.get(enclosingElementKey);
            if (enclosingElementBinding == null){
                enclosingElementBinding = new EnclosingElementBinding();
                enclosingElementBinding.element = element;
                enclosingElementBindingMap.put(enclosingElementKey, enclosingElementBinding);
            }
            Map<Class<?>, TypeElementBinding> typeElementBindingMap = enclosingElementBinding.typeElementBindingMap;
            TypeElementBinding typeElementBinding = typeElementBindingMap.get(OnClick.class);
            if (typeElementBinding == null){
                typeElementBinding = new TypeElementBinding();
                typeElementBinding.typeElement = OnClick.class;
                typeElementBindingMap.put(OnClick.class, typeElementBinding);
            }
            List<BindingElement> bindElementList = typeElementBinding.bindingElementList;
            BindingElement bindingElement = new BindingElement();
            bindingElement.objectName = element.getSimpleName().toString();
            bindingElement.value = element.getAnnotation(OnClick.class).id();
            bindElementList.add(bindingElement);
        }
    }

    private String getPackageName(Element enclosingElement) {
        String temp = enclosingElement.toString();
        int lastIndex = temp.lastIndexOf(".");

        return temp.substring(0, lastIndex);
    }
}
