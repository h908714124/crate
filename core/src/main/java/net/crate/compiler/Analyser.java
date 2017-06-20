package net.crate.compiler;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.CrateProcessor.rawType;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.annotation.Generated;

final class Analyser {

  private final Model model;
  private final List<ParaParameter> properties;

  private final FieldSpec staticInstanceField;

  private Analyser(
      Model model,
      List<ParaParameter> properties) {
    this.model = model;
    this.properties = properties;
    this.staticInstanceField = staticInstanceField(model);
  }

  static Analyser create(
      TypeScanner typeScanner) {
    return new Analyser(
        typeScanner.model,
        typeScanner.properties());
  }

  TypeSpec analyse() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(
        rawType(model.generatedClass))
        .addField(staticInstanceField)
        .addMethod(staticBuilderMethod())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addModifiers(FINAL)
        .addModifiers(model.maybePublic())
        .addAnnotation(generatedAnnotation());
    if (properties.isEmpty()) {
      return builder.build();
    }
    List<StepDef> stepDefs = steps(model, properties);
    builder.addMethods(stepDefs.get(0).nextMethods());
    stepDefs.stream().skip(1).map(StepDef::typeSpec)
        .forEach(builder::addType);
    return builder.build();
  }

  private static FieldSpec staticInstanceField(Model model) {
    return FieldSpec.builder(model.generatedClass, "INSTANCE")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .initializer(CodeBlock.of("new $T()", model.generatedClass))
        .build();
  }

  private MethodSpec staticBuilderMethod() {
    return MethodSpec.methodBuilder("builder")
        .addStatement("return $N", staticInstanceField)
        .returns(model.generatedClass)
        .addModifiers(STATIC)
        .build();
  }

  private static List<StepDef> steps(
      Model model,
      List<ParaParameter> properties) {
    GenericsImpl genericsImpl = new GenericsImpl(model, properties);
    return genericsImpl.stepImpls();
  }

  private AnnotationSpec generatedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S",
            CrateProcessor.class.getCanonicalName())
        .build();
  }
}
