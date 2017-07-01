package net.crate.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.crate.compiler.ParaParameter.GET_PROPERTY;
import static net.crate.compiler.Util.joinCodeBlocks;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

final class ConvenienceNext extends ParaParameter.Cases<ConvenienceNext.NextResult, Integer> {

  private final Model model;
  private final List<ParaParameter> properties;

  static final class NextResult {
    final List<MethodSpec> methods;
    final List<TypeSpec> extraTypes;
    NextResult(List<MethodSpec> methods, List<TypeSpec> extraTypes) {
      this.methods = methods;
      this.extraTypes = extraTypes;
    }
  }

  ConvenienceNext(Model model, List<ParaParameter> properties) {
    this.model = model;
    this.properties = properties;
  }

  @Override
  ConvenienceNext.NextResult property(Property property, Integer i) {
    return new NextResult(singletonList(nextMethod(i)), emptyList());
  }

  @Override
  ConvenienceNext.NextResult collectionish(Collectionish collectionish, Integer i) {
    return new NextResult(singletonList(nextMethod(i)), emptyList());
  }

  @Override
  ConvenienceNext.NextResult optionalish(Optionalish optionalish, Integer i) {
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

    return new NextResult(Arrays.asList(nextMethod(i), methodSpec), emptyList());
  }

  private TypeName nextStepType(int i) {
    return nextStepType(model, properties, i);
  }

  private static TypeName nextStepType(
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

  private MethodSpec nextMethod(int i) {
    ParameterSpec parameter = ParameterSpec.builder(
        get(i).type(),
        get(i).name()).build();
    return methodBuilder(
        get(i).name())
        .addParameter(parameter)
        .addTypeVariables(model.varLife.methodParams.get(i))
        .addModifiers(model.maybePublic())
        .returns(nextStepType(model, properties, i))
        .addCode(nextBlock(i, parameter))
        .addExceptions(i == properties.size() - 1 ?
            model.thrownTypes :
            emptyList())
        .build();
  }

  private CodeBlock nextBlock(int i, ParameterSpec parameter) {
    if (i == properties.size() - 1) {
      return constructorInvocation();
    }
    TypeName next = parameterizedTypeName(model.generatedClass.nestedClass(
        upcase(get(i).name())),
        model.varLife.typeParams.get(i));
    return i == 0 ?
        CodeBlock.builder()
            .addStatement("return new $T($N)", next, parameter)
            .build() :
        CodeBlock.builder()
            .addStatement("return new $T(this, $N)", next, parameter)
            .build();
  }


  private CodeBlock constructorInvocation() {
    boolean fewParameters = properties.size() < 3;
    CodeBlock invoke = IntStream.range(0, properties.size())
        .mapToObj(this::invokeFn)
        .collect(fewParameters ?
            joinCodeBlocks(", ") :
            joinCodeBlocks(",\n"));
    String lineBreak = fewParameters ? "" : "\n";
    return CodeBlock.builder().addStatement(
        "return new $T($L$L)", model.targetClass, lineBreak, invoke)
        .build();
  }


  private CodeBlock invokeFn(int i) {
    CodeBlock.Builder block = CodeBlock.builder();
    if (i == properties.size() - 1) {
      String name = get(i).name();
      block.add("$L", name);
      return block.build();
    }
    for (int j = properties.size() - 3; j >= i; j--) {
      block.add("up.");
    }
    block.add(get(i).name());
    return block.build();
  }

  private Property get(int i) {
    return GET_PROPERTY.apply(properties.get(i));
  }
}
