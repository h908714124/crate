package net.crate.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.Generated;
import java.util.List;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
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
    TypeSpec.Builder builder = TypeSpec.classBuilder(rawType(model.generatedClass));
    steps(model).forEach(builder::addType);
    return builder.addModifiers(model.maybePublic())
        .addModifiers(ABSTRACT)
        .addTypeVariables(model.typevars())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .build())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", CrateProcessor.class.getCanonicalName())
            .build())
        .build();
  }

  private static List<TypeSpec> steps(Model model) {
    List<TypeVariableName> typeParameters = model.typevars();
    VarLife varLife = VarLife.create(
        typeParameters,
        stepTypes(model));
    GenericsImpl genericsImpl = new GenericsImpl(model);
    return genericsImpl.stepImpls(varLife);
  }
}
