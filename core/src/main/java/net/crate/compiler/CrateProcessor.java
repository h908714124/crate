package net.crate.compiler;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.util.ElementFilter.typesIn;
import static javax.tools.Diagnostic.Kind.ERROR;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import net.crate.AutoCrate;
import net.crate.Crate;

public final class CrateProcessor extends AbstractProcessor {

  private static final String AV_PREFIX = "AutoValue_";
  private static final String DOUBLE_ERROR = "class cannot have both @Crate and @AutoCrate";
  private static final String SUFFIX = "_Crate";

  private final Set<String> deferredTypeNames = new HashSet<>();
  private final Set<String> done = new HashSet<>();

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
    try {
      processCrates(env);
      processAutoCrates(env);
    } catch (ValidationException e) {
      processingEnv.getMessager().printMessage(
          ERROR, e.getMessage(), e.about);
    } catch (Exception e) {
      String trace = getStackTraceAsString(e);
      String message = "Unexpected error: " + trace;
      processingEnv.getMessager().printMessage(ERROR, message);
    }
    return false;
  }

  private void processCrates(RoundEnvironment env) throws IOException {
    Set<TypeElement> typeElements =
        typesIn(env.getElementsAnnotatedWith(Crate.class));
    for (TypeElement sourceClassElement : typeElements) {
      String key = sourceClassElement.getQualifiedName().toString();
      if (done.contains(key)) {
        continue;
      }
      if (sourceClassElement.getAnnotation(AutoCrate.class) != null) {
        throw new ValidationException(DOUBLE_ERROR, sourceClassElement);
      }
      ClassName generatedClass = cratePeer(sourceClassElement);
      TypeScanner typeScanner = Model.create(
          sourceClassElement, sourceClassElement, generatedClass);
      Analyser analyser = Analyser.create(typeScanner);
      TypeSpec typeSpec = analyser.analyse();
      write(rawType(generatedClass), typeSpec);
      done.add(key);
    }
  }

  private void processAutoCrates(RoundEnvironment env) throws IOException {
    List<TypeElement> deferredTypes = deferredTypeNames.stream()
        .map(name -> processingEnv.getElementUtils().getTypeElement(name))
        .collect(toList());
    if (env.processingOver()) {
      for (String type : deferredTypeNames) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Could not find auto-value impl of " + type);
      }
      return;
    }
    List<TypeElement> types = Stream.of(
        deferredTypes,
        typesIn(env.getElementsAnnotatedWith(AutoCrate.class)))
        .flatMap(Collection::stream)
        .collect(toList());
    deferredTypeNames.clear();

    for (TypeElement sourceClassElement : types) {
      String key = sourceClassElement.getQualifiedName().toString();
      if (done.contains(key)) {
        continue;
      }
      if (sourceClassElement.getAnnotation(Crate.class) != null) {
        throw new ValidationException(DOUBLE_ERROR, sourceClassElement);
      }
      TypeElement targetClassElement = processingEnv.getElementUtils().getTypeElement(
          autoValuePeer(sourceClassElement).toString());
      if (targetClassElement == null) {
        // Auto-value hasn't written its class yet.
        deferredTypeNames.add(sourceClassElement.getQualifiedName().toString());
        continue;
      }
      ClassName generatedClass = cratePeer(sourceClassElement);
      TypeScanner typeScanner = Model.create(
          sourceClassElement, targetClassElement, generatedClass);
      Analyser analyser = Analyser.create(typeScanner);
      TypeSpec typeSpec = analyser.analyse();
      write(rawType(generatedClass), typeSpec);
      done.add(key);
    }
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

  private static ClassName autoValuePeer(TypeElement typeElement) {
    TypeName type = TypeName.get(typeElement.asType());
    String name = AV_PREFIX + String.join("_", rawType(type).simpleNames());
    return rawType(type).topLevelClassName().peerClass(name);
  }

  static ClassName rawType(TypeName typeName) {
    if (typeName instanceof TypeVariableName) {
      return TypeName.OBJECT;
    }
    if (typeName.getClass().equals(TypeName.class)) {
      return TypeName.OBJECT;
    }
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

  private static String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
}
