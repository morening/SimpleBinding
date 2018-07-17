package com.morening.android.processor;

import com.morening.android.annotation.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.morening.android.annotation.OnClick")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor{
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element: roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {
            String packageName = element.getEnclosingElement().toString();
            String className = element.getEnclosingElement().getSimpleName().toString();

            StringBuilder builder = new StringBuilder()
                    .append("package com.morening.android.simplebinding;\n\n")
                    .append("import android.view.View;\n")
                    .append("import com.morening.android.simplebinding.R;\n")
                    .append("import " + packageName + ";\n\n")
                    .append("public class " + className + "_SimpleBinding {\n\n")
                    .append("\tpublic " + className + "_SimpleBinding(final " + className + " target, View source){\n\n")
                    .append("\t\tsource.findViewById(")
                    .append(element.getAnnotation(OnClick.class).viewId())
                    .append(").setOnClickListener(new View.OnClickListener(){\n\n")
                    .append("\t\t\t@Override\n")
                    .append("\t\t\tpublic void onClick(View view){\n")
                    .append("\t\t\t\ttarget.onClick(view);\n")
                    .append("\t\t\t}\n")
                    .append("\t\t});\n")
                    .append("\t}\n")
                    .append("}");

            try {
                JavaFileObject source = processingEnv.getFiler().createSourceFile("com.morening.android.simplebinding."+ className +"_SimpleBinding");
                Writer writer = source.openWriter();
                writer.write(builder.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {

            }
        }

        return true;
    }
}
