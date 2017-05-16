package net.crate.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.Generated;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.CrateProcessor.rawType;
import static net.crate.compiler.GenericsContract.stepTypes;

final class Analyser {

  private final Model model;

  private Analyser(Model model) {
    this.model = model;
  }

  static Analyser create(Model model) {
    return new Analyser(model);
  }

  TypeSpec analyse() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(
        rawType(model.generatedClass));
    builder.addModifiers(model.maybePublic())
        .addModifiers(FINAL)
        .addModifiers(model.maybePublic())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addTypeVariables(model.typevars())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S",
                CrateProcessor.class.getCanonicalName())
            .build());
    builder.addMethod(staticBuilderMethod());
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

  private MethodSpec staticBuilderMethod() {
    return MethodSpec.methodBuilder("builder")
        .addStatement("return new $T()", model.generatedClass)
        .returns(model.generatedClass)
        .addModifiers(STATIC)
        .build();
  }

  private static List<StepDef> steps(Model model) {
    List<TypeVariableName> typeParameters = model.typevars();
    VarLife varLife = VarLife.create(
        typeParameters,
        stepTypes(model));
    GenericsImpl genericsImpl = new GenericsImpl(model);
    return genericsImpl.stepImpls(varLife);
  }
}
