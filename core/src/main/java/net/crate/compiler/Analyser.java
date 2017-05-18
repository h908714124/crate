package net.crate.compiler;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.CrateProcessor.rawType;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.annotation.Generated;

final class Analyser {

  private final Model model;

  private Analyser(Model model) {
    this.model = model;
  }

  static Analyser create(Model model) {
    return new Analyser(model);
  }

  static TypeSpec.Builder stub(ClassName generatedClass) {
    TypeSpec.Builder builder = TypeSpec.classBuilder(
        rawType(generatedClass));
    return builder.addModifiers(FINAL)
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S",
                CrateProcessor.class.getCanonicalName())
            .build())
        .addMethod(staticBuilderMethod(generatedClass));
  }

  TypeSpec analyse() {
    TypeSpec.Builder builder = stub(model.generatedClass);
    builder.addModifiers(model.maybePublic());
    if (model.properties.isEmpty()) {
      return builder.build();
    }
    List<StepDef> stepDefs = steps(model);
    if (!model.properties.isEmpty()) {
      builder.addMethods(stepDefs.get(0).nextMethods());
    }
    stepDefs.stream().skip(1).map(StepDef::typeSpec)
        .forEach(builder::addType);
    return builder.build();
  }

  private static MethodSpec staticBuilderMethod(ClassName generatedClass) {
    return MethodSpec.methodBuilder("builder")
        .addStatement("return new $T()", generatedClass)
        .returns(generatedClass)
        .addModifiers(STATIC)
        .build();
  }

  private static List<StepDef> steps(Model model) {
    GenericsImpl genericsImpl = new GenericsImpl(model);
    return genericsImpl.stepImpls();
  }
}
