package net.crate.compiler;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.Util.joinCodeBlocks;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.lang.model.element.Modifier;

final class GenericsImpl {

  private static final ClassName AUTO_VALUE = ClassName.get(
      "com.google.auto.value", "AutoValue");
  private static final Modifier[] NO_MODIFIERS = new Modifier[0];
  private static final Modifier[] ONLY_FINAL = {FINAL};

  private final Model model;

  GenericsImpl(Model model) {
    this.model = model;
  }

  List<StepDef> stepImpls() {
    List<StepDef> builder = new ArrayList<>(model.properties.size());
    ImplFields implFields = ImplFields.create(model);
    for (int i = 0; i < model.properties.size(); i++) {
      builder.add(stepDef(implFields, i));
    }
    return builder;
  }

  private StepDef stepDef(
      ImplFields implFields,
      int i) {
    List<MethodSpec> fields = implFields.fields(i);
    List<MethodSpec> nextMethods = new ArrayList<>(2);
    nextMethods.add(nextMethod(i));
    model.properties.get(i).optionalish()
        .ifPresent(optionalish -> nextMethods.add(
            convenienceNextMethod(i, optionalish))
        );
    TypeSpec typeSpec = i == 0 ? null : TypeSpec.classBuilder(
        upcase(model.properties.get(i - 1).name()))
        .addMethods(fields)
        .addTypeVariables(model.varLife.typeParams.get(i - 1))
        .addMethods(nextMethods)
        .addAnnotation(AUTO_VALUE)
        .addModifiers(ABSTRACT, STATIC)
        .addModifiers(model.maybePublic())
        .build();
    return new StepDef(typeSpec, nextMethods);
  }

  private MethodSpec nextMethod(int i) {
    ParameterSpec parameter = ParameterSpec.builder(
        model.properties.get(i).type(),
        model.properties.get(i).name()).build();
    return methodBuilder(
        model.properties.get(i).name())
        .addParameter(parameter)
        .addTypeVariables(model.varLife.methodParams.get(i))
        .addModifiers(model.maybePublic())
        .returns(nextStepType(i))
        .addCode(nextBlock(i, parameter))
        .addModifiers(maybeFinal(i))
        .addExceptions(i == model.properties.size() - 1 ?
            model.thrownTypes :
            emptyList())
        .build();
  }

  private MethodSpec convenienceNextMethod(
      int i, Optionalish optionalish) {
    ParameterSpec parameter = ParameterSpec.builder(
        optionalish.wrapped,
        model.properties.get(i).name()).build();
    return methodBuilder(
        model.properties.get(i).name())
        .addParameter(parameter)
        .addTypeVariables(model.varLife.methodParams.get(i))
        .addModifiers(model.maybePublic())
        .returns(nextStepType(i))
        .addCode(convenienceNextBlock(i, parameter, optionalish))
        .addExceptions(i == model.properties.size() - 1 ?
            model.thrownTypes :
            emptyList())
        .build();
  }

  private TypeName nextStepType(int i) {
    if (i == model.properties.size() - 1) {
      return model.sourceClass();
    }
    ClassName rawNext = model.generatedClass
        .nestedClass(upcase(
            model.properties.get(i).name()));
    return parameterizedTypeName(rawNext, model.varLife.typeParams.get(i));
  }

  private CodeBlock nextBlock(int i, ParameterSpec parameter) {
    if (i == model.properties.size() - 1) {
      return constructorInvocation();
    }
    TypeName next = parameterizedTypeName(model.generatedClass.peerClass(
        "AutoValue_" + model.generatedClass.simpleName() + "_" +
            upcase(model.properties.get(i).name())),
        model.varLife.typeParams.get(i));
    return i == 0 ?
        CodeBlock.builder()
            .addStatement("return new $T($N)", next, parameter)
            .build() :
        CodeBlock.builder()
            .addStatement("return new $T(this, $N)", next, parameter)
            .build();
  }

  private CodeBlock convenienceNextBlock(
      int i, ParameterSpec parameter, Optionalish optionalish) {
    if (i == model.properties.size() - 1) {
      return CodeBlock.builder()
          .addStatement("return $L($T.$L($N))",
              model.properties.get(i).name(),
              optionalish.wrapper,
              optionalish.ofLiteral(),
              parameter)
          .build();
    }
    ClassName next = model.generatedClass.peerClass(
        "AutoValue_" + model.generatedClass.simpleName() + "_" +
            upcase(model.properties.get(i).name()));
    return i == 0 ?
        CodeBlock.builder()
            .addStatement("return new $T($T.$L($N))",
                next,
                optionalish.wrapper,
                optionalish.ofLiteral(),
                parameter)
            .build() :
        CodeBlock.builder()
            .addStatement("return new $T(this, $T.$L($N))",
                next,
                optionalish.wrapper,
                optionalish.ofLiteral(),
                parameter)
            .build();
  }

  private CodeBlock constructorInvocation() {
    CodeBlock invoke = IntStream.range(0, model.properties.size())
        .mapToObj(this::invokeFn)
        .collect(joinCodeBlocks(",\n"));
    return CodeBlock.builder().addStatement(
        "return new $T(\n$L)", model.targetClass, invoke)
        .build();
  }

  private CodeBlock invokeFn(int i) {
    CodeBlock.Builder block = CodeBlock.builder();
    if (i == model.properties.size() - 1) {
      String name = model.properties.get(i).name();
      block.add("$L", name);
      return block.build();
    }
    for (int j = model.properties.size() - 3; j >= i; j--) {
      block.add("$L().", "ref");
    }
    block.add("$L()", "get");
    return block.build();
  }

  private Modifier[] maybeFinal(int i) {
    return i == 0 ? NO_MODIFIERS : ONLY_FINAL;
  }
}
