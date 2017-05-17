package net.crate.compiler;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.typesIn;
import static javax.tools.Diagnostic.Kind.ERROR;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import net.crate.AutoCrate;
import net.crate.Crate;

public final class CrateProcessor extends AbstractProcessor {

  private static final String AV_PREFIX = "AutoValue_";
  private static final String DOUBLE_ERROR = "class cannot have both @Crate and @AutoCrate";
  private static final String SUFFIX = "_Crate";

  private final Set<TypeName> done = new HashSet<>();
  private final Set<TypeElement> seen = new HashSet<>();

  private boolean errorCaught = false;

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> strings = new HashSet<>();
    strings.add(Crate.class.getCanonicalName());
    strings.add(AutoCrate.class.getCanonicalName());
    return strings;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    if (errorCaught) {
      return false;
    }
    try {
      processCrate(env);
      processAutoCrate(env);
    } catch (ValidationException e) {
      errorCaught = true;
      processingEnv.getMessager().printMessage(
          ERROR, e.getMessage(), e.about);
    } catch (GeneralError e) {
      errorCaught = true;
      handleException(e.context, e.getCause());
    }
    return false;
  }

  private void processCrate(RoundEnvironment env) throws GeneralError {
    Set<TypeElement> typeElements =
        typesIn(env.getElementsAnnotatedWith(Crate.class));
    for (TypeElement sourceClassElement : typeElements) {
      if (sourceClassElement.getAnnotation(AutoCrate.class) != null) {
        throw new ValidationException(DOUBLE_ERROR, sourceClassElement);
      }
      TypeName sourceClass = TypeName.get(sourceClassElement.asType());
      try {
        if (!done.add(sourceClass)) {
          continue;
        }
        Model model = Model.create(sourceClassElement, cratePeer(sourceClassElement));
        TypeSpec typeSpec = Analyser.create(model).analyse();
        write(rawType(model.generatedClass), typeSpec);
      } catch (Exception e) {
        if (e instanceof ValidationException) {
          throw (ValidationException) e;
        }
        throw new GeneralError(e, sourceClassElement);
      }
    }
  }

  private void processAutoCrate(RoundEnvironment env) throws GeneralError {
    for (TypeElement sourceClassElement : seen) {
      if (sourceClassElement.getAnnotation(Crate.class) != null) {
        throw new ValidationException(DOUBLE_ERROR, sourceClassElement);
      }
      TypeName sourceClass = TypeName.get(sourceClassElement.asType());
      if (!done.add(sourceClass)) {
        continue;
      }
      ClassName generatedByAutoValue = avPeer(sourceClass);
      TypeElement avType = processingEnv.getElementUtils().getTypeElement(
          generatedByAutoValue.toString());
      try {
        if (avType == null) {
          // Auto-value hasn't written its class yet.
          // Leave a placeholder, to notify the user.
          // This will hopefully be overwritten in a future round.
          writePlaceholder(sourceClassElement, generatedByAutoValue);
          continue;
        }
        Model model = Model.create(avType, cratePeer(sourceClassElement));
        TypeSpec typeSpec = Analyser.create(model).analyse();
        write(rawType(model.generatedClass), typeSpec);
      } catch (Exception e) {
        if (e instanceof ValidationException) {
          throw (ValidationException) e;
        }
        throw new GeneralError(e, sourceClassElement);
      }
    }
    // Don't even try to do anything in the first round.
    // Just remember these type elements, so we can handle them later.
    Set<TypeElement> typeElements =
        typesIn(env.getElementsAnnotatedWith(AutoCrate.class));
    seen.addAll(typeElements);
  }

  private void writePlaceholder(
      TypeElement sourceClassElement,
      ClassName generatedByAutoValue) throws IOException {
    TypeName generatedClass = cratePeer(sourceClassElement);
    TypeSpec typeSpec = TypeSpec.classBuilder(rawType(generatedClass))
        .addModifiers(Modifier.ABSTRACT)
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", CrateProcessor.class.getCanonicalName())
            .build())
        .addMethod(MethodSpec.methodBuilder("builder")
            .addModifiers(STATIC, PRIVATE)
            .returns(generatedClass)
            .addStatement("throw new $T(\n$S + \n$S)",
                UnsupportedOperationException.class,
                generatedByAutoValue.simpleName() + " not found. ",
                "Maybe auto-value is not configured?")
            .build())
        .build();
    write(rawType(generatedClass), typeSpec);
  }

  private void handleException(TypeElement typeElement, Throwable e) {
    String message = "Unexpected error while processing " +
        ClassName.get(typeElement) +
        ": " + e.getMessage();
    processingEnv.getMessager().printMessage(ERROR, message, typeElement);
  }

  private void write(ClassName generatedType, TypeSpec typeSpec) throws IOException {
    JavaFile javaFile = JavaFile.builder(generatedType.packageName(), typeSpec)
        .skipJavaLangImports(true)
        .build();
    JavaFileObject sourceFile = processingEnv.getFiler()
        .createSourceFile(generatedType.toString(),
            javaFile.typeSpec.originatingElements.toArray(new Element[0]));
    try (Writer writer = sourceFile.openWriter()) {
      writer.write(javaFile.toString());
    }
  }

  private static ClassName avPeer(TypeName type) {
    String name = AV_PREFIX + String.join("_", rawType(type).simpleNames());
    return rawType(type).topLevelClassName().peerClass(name);
  }

  static ClassName rawType(TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) typeName).rawType;
    }
    return ((ClassName) typeName);
  }

  private static ClassName cratePeer(TypeElement sourceClassElement) {
    TypeName type = TypeName.get(sourceClassElement.asType());
    String name = String.join("_", rawType(type).simpleNames()) + SUFFIX;
    return rawType(type).topLevelClassName().peerClass(name);
  }
}
