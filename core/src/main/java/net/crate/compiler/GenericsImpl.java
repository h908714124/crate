package net.crate.compiler;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.GenericsContract.nextStepType;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class GenericsImpl {

  private static final ClassName AUTO_VALUE = ClassName.get(
      "com.google.auto.value", "AutoValue");

  private final Model model;

  GenericsImpl(Model model) {
    this.model = model;
  }

  List<StepDef> stepImpls(VarLife varLife) {
    List<StepDef> builder = new ArrayList<>(model.properties.size());
    ImplFields implFields = ImplFields.create(model, varLife.typeParams);
    for (int i = 0; i < model.properties.size(); i++) {
      builder.add(stepDef(implFields, varLife, i));
    }
    return builder;
  }

  private StepDef stepDef(
      ImplFields implFields,
      VarLife varLife,
      int i) {
    ParameterSpec parameter = ParameterSpec.builder(
        model.properties.get(i).type(),
        model.properties.get(i).name()).build();
    List<MethodSpec> fields = implFields.fields(i);
    List<MethodSpec.Builder> nextMethodBuilders = new ArrayList<>();
    nextMethodBuilders.add(methodBuilder(
        model.properties.get(i).name())
        .addParameter(parameter)
        .addTypeVariables(varLife.methodParams.get(i))
        .addModifiers(model.maybePublic())
        .returns(nextStepType(model, varLife.typeParams, i))
        .addCode(getCodeBlock(i, varLife, parameter))
        .addExceptions(i == model.properties.size() - 1 ?
            model.thrownTypes :
            emptyList()));
    OptionalInfo.create(model.properties.get(i).type())
        .ifPresent(optionalInfo -> {
              ParameterSpec wrappedParameter = ParameterSpec.builder(
                  optionalInfo.wrapped,
                  model.properties.get(i).name()).build();
              nextMethodBuilders.add(methodBuilder(
                  model.properties.get(i).name())
                  .addParameter(wrappedParameter)
                  .addTypeVariables(varLife.methodParams.get(i))
                  .addModifiers(model.maybePublic())
                  .returns(nextStepType(model, varLife.typeParams, i))
                  .addCode(getOptionalCodeBlock(i, wrappedParameter, optionalInfo))
                  .addExceptions(i == model.properties.size() - 1 ?
                      model.thrownTypes :
                      emptyList()));
            }
        );
    List<MethodSpec> nextMethods = nextMethods(i, nextMethodBuilders);
    TypeSpec typeSpec = i == 0 ? null : TypeSpec.classBuilder(
        upcase(model.properties.get(i - 1).name()))
        .addMethods(fields)
        .addTypeVariables(varLife.typeParams.get(i))
        .addMethods(nextMethods)
        .addAnnotation(AUTO_VALUE)
        .addModifiers(ABSTRACT, STATIC)
        .addModifiers(model.maybePublic())
        .build();
    return new StepDef(typeSpec, nextMethods);
  }

  private List<MethodSpec> nextMethods(int i, List<MethodSpec.Builder> nextMethodBuilders) {
    List<MethodSpec> nextMethods;
    if (i == 0) {
      nextMethods = nextMethodBuilders.stream()
          .map(MethodSpec.Builder::build)
          .collect(Collectors.toList());
    } else {
      nextMethods = nextMethodBuilders.stream()
          .map(builder -> builder.addModifiers(FINAL))
          .map(MethodSpec.Builder::build)
          .collect(Collectors.toList());
    }
    return nextMethods;
  }

  private CodeBlock getCodeBlock(int i, VarLife varLife, ParameterSpec parameter) {
    if (i == model.properties.size() - 1) {
      return fullInvoke();
    }
    TypeName next = parameterizedTypeName(model.generatedClass.peerClass(
        "AutoValue_" + model.generatedClass.simpleName() + "_" +
            upcase(model.properties.get(i).name())),
        varLife.typeParams.get(i + 1));
    if (i == 0) {
      return CodeBlock.builder()
          .addStatement("return new $T($N)", next, parameter)
          .build();
    }
    return CodeBlock.builder()
        .addStatement("return new $T(this, $N)", next, parameter)
        .build();
  }

  private CodeBlock getOptionalCodeBlock(
      int i, ParameterSpec parameter, OptionalInfo optionalInfo) {
    if (i == model.properties.size() - 1) {
      return fullInvoke();
    }
    ClassName next = model.generatedClass.peerClass(
        "AutoValue_" + model.generatedClass.simpleName() + "_" + upcase(model.properties.get(i).name()));
    CodeBlock.Builder parameterLiteral = CodeBlock.builder();
    if (optionalInfo.isOptional()) {
      parameterLiteral.add("$T.ofNullable($N)",
          Optional.class, parameter);
    } else {
      parameterLiteral.add("$T.of($N)",
          optionalInfo.wrapper, parameter);
    }
    if (i == 0) {
      return CodeBlock.builder()
          .addStatement("return new $T($L)", next, parameterLiteral.build())
          .build();
    }
    return CodeBlock.builder()
        .addStatement("return new $T(this, $L)", next, parameterLiteral.build())
        .build();
  }

  private CodeBlock fullInvoke() {
    CodeBlock invoke = IntStream.range(0, model.properties.size())
        .mapToObj(this::invokeFn)
        .collect(joinCodeBlocks(",\n"));
    return CodeBlock.builder().addStatement(
        "return new $T(\n$L)",
        model.sourceClass, invoke).build();
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
    block.add("$L()", "data");
    return block.build();
  }
}
