package net.crate.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.GenericsContract.nextStepType;
import static net.crate.compiler.Util.joinCodeBlocks;
import static net.crate.compiler.Util.upcase;

final class GenericsImpl {

  private static final ClassName AUTO_VALUE = ClassName.get(
      "com.google.auto.value", "AutoValue");

  private final Model model;

  GenericsImpl(Model model) {
    this.model = model;
  }

  List<TypeSpec> stepImpls(VarLife varLife) {
    List<List<TypeVariableName>> typeParams = varLife.typeParams();
    List<List<TypeVariableName>> methodParams = varLife.methodParams();
    List<TypeSpec> builder = new ArrayList<>(model.properties.size());
    ImplFields implFields = new ImplFields(model, typeParams);
    for (int i = 0; i < model.properties.size(); i++) {
      builder.add(createStep(implFields, methodParams, typeParams, i));
    }
    return builder;
  }

  private TypeSpec createStep(ImplFields implFields,
                              List<List<TypeVariableName>> methodParams,
                              List<List<TypeVariableName>> typeParams, int i) {
    ParameterSpec parameter = ParameterSpec.builder(
        model.properties.get(i).type(),
        model.properties.get(i).name()).build();
    List<MethodSpec> fields = implFields.fields(i);
    TypeSpec.Builder builder = TypeSpec.classBuilder(
        upcase(model.properties.get(i).name()));
    return builder.addMethods(fields)
        .addTypeVariables(typeParams.get(i))
        .addMethod(methodBuilder(model.properties.get(i).name())
            .addParameter(parameter)
            .addTypeVariables(methodParams.get(i))
            .addModifiers(FINAL)
            .returns(nextStepType(model, typeParams, i))
            .addCode(getCodeBlock(i, parameter))
            .addExceptions(i == model.properties.size() - 1 ?
                model.thrownTypes :
                emptyList())
            .build())
        .addAnnotation(AUTO_VALUE)
        .addModifiers(STATIC, ABSTRACT)
        .build();
  }

  private CodeBlock getCodeBlock(int i, ParameterSpec parameter) {
    if (i == model.properties.size() - 1) {
      return fullInvoke();
    }
    ClassName next = model.generatedClass.peerClass(
        "AutoValue_" + model.generatedClass.simpleName() + "_" + upcase(model.properties.get(i + 1).name()));
    if (i == 0) {
      return CodeBlock.builder()
          .addStatement("return new $T($N)", next, parameter)
          .build();
    }
    return CodeBlock.builder()
        .addStatement("return new $T(this, $N)", next, parameter)
        .build();
  }

  private CodeBlock fullInvoke() {
    CodeBlock invoke = IntStream.range(0, model.properties.size())
        .mapToObj(invokeFn())
        .collect(joinCodeBlocks(",\n    "));
    return CodeBlock.builder().addStatement(
        "return new $T(\n    $L)",
        model.sourceClass, invoke).build();
  }

  private IntFunction<CodeBlock> invokeFn() {
    return i -> {
      CodeBlock.Builder block = CodeBlock.builder();
      if (i == model.properties.size() - 1) {
        String name = model.properties.get(i).name();
        block.add("$L", name);
        return block.build();
      }
      for (int j = model.properties.size() - 3; j >= i; j--) {
        block.add("$L().", "pointer");
      }
      block.add("$L()", "data");
      return block.build();
    };
  }

}
