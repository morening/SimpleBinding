package com.morening.android.processor;

import com.morening.android.annotation.BindView;
import com.morening.android.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
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

//        Map<Element, Map<TypeElement, >>
//        collectAndParseAllAnnotations(set, roundEnvironment);

        for (Element element: roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {
            String packageName = getPackageName(element.getEnclosingElement());
            String targetName = element.getEnclosingElement().getSimpleName().toString();
            ClassName targetClassName = ClassName.get(packageName, targetName);
            ClassName onClickListenerClassName = ClassName.get("android.view.View", "OnClickListener");
            ClassName unbinderClassName = ClassName.get("com.morening.android.simplebinding", "Unbinder");

            try {
                TypeSpec onClickListener = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(onClickListenerClassName)
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(VIEW, "view")
                                .addStatement("$N.onClick(view)", "target")
                                .build())
                        .build();
                MethodSpec constructor = MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(targetClassName, "target", Modifier.FINAL).build())
                        .addParameter(ParameterSpec.builder(VIEW, "source").build())
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("$N.findViewById($L).setOnClickListener($L);",
                                "source",
                                element.getAnnotation(OnClick.class).id(),
                                onClickListener)
                        .build();

                MethodSpec unbind = MethodSpec.methodBuilder("unbind")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addAnnotation(Override.class)
                        .build();

                TypeSpec clazzSpec = TypeSpec.classBuilder(element.getEnclosingElement().getSimpleName()+"_SimpleBinding")
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(unbinderClassName)
                        .addMethod(constructor)
                        .addMethod(unbind)
                        .build();

                JavaFile javaFile = JavaFile.builder("com.morening.android.simplebinding", clazzSpec).build();
                javaFile.writeTo(mFiler);
            } catch (IOException e) {

            }
        }

        return true;
    }

    private String getPackageName(Element enclosingElement) {
        String temp = enclosingElement.toString();
        int lastIndex = temp.lastIndexOf(".");

        return temp.substring(0, lastIndex);
    }
}
