package com.morening.android.processor;

import com.morening.android.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("com.morening.android.annotation.OnClick")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor{

    private Filer mFiler = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {

            ClassName targetName = ClassName.get("com.morening.android.simplebinding", "MainActivity");
            ClassName sourceName = ClassName.get("android.view", "View");
            ClassName onClickListenerClass = ClassName.get("android.view.View", "OnClickListener");

            try {
                TypeSpec onClickListener = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(onClickListenerClass)
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(sourceName, "view")
                                .addStatement("$N.onClick(view)", "target")
                                .build())
                        .build();
                MethodSpec constructor = MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(targetName, "target", Modifier.FINAL).build())
                        .addParameter(ParameterSpec.builder(sourceName, "source").build())
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("$N.findViewById($L).setOnClickListener($L);",
                                "source",
                                element.getAnnotation(OnClick.class).viewId(),
                                onClickListener)
                        .build();

                TypeSpec clazzSpec = TypeSpec.classBuilder(element.getEnclosingElement().getSimpleName()+"_SimpleBinding")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor)
                        .build();

                JavaFile javaFile = JavaFile.builder("com.morening.android.simplebinding", clazzSpec).build();
                javaFile.writeTo(mFiler);
            } catch (IOException e) {

            }
        }

        return true;
    }
}
