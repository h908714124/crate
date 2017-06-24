package net.crate.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.List;
import java.util.Optional;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static net.crate.compiler.ParaParameter.GET_PROPERTY;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

final class ConvenienceNext extends ParaParameter.Cases<Optional<MethodSpec>, Integer> {

  private final Model model;
  private final List<ParaParameter> properties;

  ConvenienceNext(Model model, List<ParaParameter> properties) {
    this.model = model;
    this.properties = properties;
  }

  @Override
  Optional<MethodSpec> property(Property property, Integer integer) {
    return empty();
  }

  @Override
  Optional<MethodSpec> optionalish(Optionalish optionalish, Integer i) {
    ParameterSpec parameter = ParameterSpec.builder(
        optionalish.wrapped,
        optionalish.property.name()).build();
    MethodSpec methodSpec = methodBuilder(
        optionalish.property.name())
        .addParameter(parameter)
        .addTypeVariables(model.varLife.methodParams.get(i))
        .addModifiers(model.maybePublic())
        .returns(nextStepType(i))
        .addCode(convenienceNextBlock(i, parameter, optionalish))
        .addExceptions(i == properties.size() - 1 ?
            model.thrownTypes :
            emptyList())
        .build();

    return Optional.of(methodSpec);
  }

  private TypeName nextStepType(int i) {
    return nextStepType(model, properties, i);
  }

  static TypeName nextStepType(
      Model model,
      List<ParaParameter> properties,
      int i) {
    if (i == properties.size() - 1) {
      return model.sourceClass();
    }
    ClassName rawNext = model.generatedClass
        .nestedClass(upcase(
            GET_PROPERTY.apply(properties.get(i)).name()));
    return parameterizedTypeName(rawNext, model.varLife.typeParams.get(i));
  }


  private CodeBlock convenienceNextBlock(
      int i, ParameterSpec parameter, Optionalish optionalish) {
    if (i == properties.size() - 1) {
      return CodeBlock.builder()
          .addStatement("return $L($T.$L($N))",
              optionalish.property.name(),
              optionalish.wrapper,
              optionalish.ofLiteral(),
              parameter)
          .build();
    }
    TypeName next = parameterizedTypeName(
        model.generatedClass.nestedClass(
            upcase(optionalish.property.name())),
        model.varLife.typeParams.get(i));
    if (i == 0) {
      return CodeBlock.builder()
          .addStatement("return new $T($T.$L($N))",
              next,
              optionalish.wrapper,
              optionalish.ofLiteral(),
              parameter)
          .build();
    }
    return CodeBlock.builder()
        .addStatement("return new $T(this, $T.$L($N))",
            next,
            optionalish.wrapper,
            optionalish.ofLiteral(),
            parameter)
        .build();
  }

}
