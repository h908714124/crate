package net.crate.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Optional;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.CrateProcessor.rawType;

final class RefTrackingBuilder {

  private final Model model;
  private final MethodSpec staticBuildMethod;
  final FieldSpec inUse;
  final ClassName refTrackingBuilderClass;
  final ClassName perThreadFactoryClass;

  private RefTrackingBuilder(Model model,
                             MethodSpec staticBuildMethod,
                             ClassName refTrackingBuilderClass,
                             ClassName perThreadFactoryClass) {
    this.model = model;
    this.staticBuildMethod = staticBuildMethod;
    this.refTrackingBuilderClass = refTrackingBuilderClass;
    this.perThreadFactoryClass = perThreadFactoryClass;
    this.inUse = FieldSpec.builder(TypeName.BOOLEAN, "inUse", PRIVATE).build();
  }

  static ClassName perThreadFactoryClass(Model model) {
    return rawType(model.generatedClass)
        .nestedClass("PerThreadFactory");
  }

  static RefTrackingBuilder create(Model model,
                                   MethodSpec staticBuildMethod) {
    return model.optionalRefTrackingBuilderClass().map(refTrackingBuilderClass -> {
      ClassName perThreadFactoryClass = perThreadFactoryClass(model);
      return new RefTrackingBuilder(model, staticBuildMethod,
          refTrackingBuilderClass, perThreadFactoryClass);
    }).orElse(null);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(refTrackingBuilderClass)
        .addField(inUse)
        .superclass(model.generatedClass)
        .addMethod(buildMethod())
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
  }

  private MethodSpec buildMethod() {
    ParameterSpec result = ParameterSpec.builder(model.sourceClass, "result").build();
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $T.$N(this)", model.sourceClass, result,
            rawType(model.generatedClass), staticBuildMethod);
    for (Property property : model.properties) {
      if (property.optionalInfo().isPresent()) {
        property.optionalInfo()
            .filter(OptionalInfo::isOptional)
            .ifPresent(optionalInfo ->
                builder.addStatement("this.$L($T.empty())",
                    property.name(), Optional.class));
      } else if (property.type() instanceof ClassName ||
          property.type() instanceof ParameterizedTypeName) {
        builder.addStatement("this.$L(null)",
            property.name());
      }
    }
    builder.addStatement("this.$N = $L", inUse, false)
        .addStatement("return $N", result);
    return MethodSpec.methodBuilder("build")
        .addAnnotation(Override.class)
        .addCode(builder.build())
        .returns(model.sourceClass)
        .addModifiers(model.maybePublic())
        .build();
  }
}