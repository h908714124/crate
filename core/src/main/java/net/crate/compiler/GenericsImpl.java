package net.crate.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.ConvenienceNext.nextStepType;
import static net.crate.compiler.ParaParameter.GET_PROPERTY;
import static net.crate.compiler.ParaParameter.asBiFunction;
import static net.crate.compiler.Util.joinCodeBlocks;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

final class GenericsImpl {

  private final Model model;
  private final List<ParaParameter> properties;

  private GenericsImpl(
      Model model,
      List<ParaParameter> properties) {
    this.model = model;
    this.properties = properties;
    this.convenienceNext =
        asBiFunction(new ConvenienceNext(model, properties));
  }

  private StepDefs stepImpls() {
    List<TypeSpec> builder = new ArrayList<>(properties.size());
    ImplFields implFields = ImplFields.create(model, properties);
    for (int i = 1; i < properties.size(); i++) {
      builder.add(stepDef(implFields, i));
    }
    return new StepDefs(nextMethods(0), builder);
  }

  static StepDefs stepImpls(
      Model model,
      List<ParaParameter> properties) {
    return new GenericsImpl(model, properties).stepImpls();
  }

  private List<MethodSpec> nextMethods(
      int i) {
    List<MethodSpec> nextMethods = new ArrayList<>(2);
    nextMethods.add(nextMethod(i));
    convenienceNext.apply(properties.get(i), i)
        .ifPresent(nextMethods::add);
    return nextMethods;
  }

  private TypeSpec stepDef(
      ImplFields implFields,
      int i) {
    return TypeSpec.classBuilder(
        upcase(get(i - 1).name()))
        .addFields(implFields.fields(i))
        .addTypeVariables(model.varLife.typeParams.get(i - 1))
        .addMethods(nextMethods(i))
        .addMethod(implFields.constructor(i))
        .addModifiers(STATIC, FINAL)
        .addModifiers(model.maybePublic())
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

  private final BiFunction<ParaParameter, Integer, Optional<MethodSpec>> convenienceNext;

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

  static final class StepDefs {
    final List<MethodSpec> initMethods;
    final List<TypeSpec> steps;
    private StepDefs(List<MethodSpec> initMethods, List<TypeSpec> steps) {
      this.initMethods = initMethods;
      this.steps = steps;
    }
  }

  private Property get(int i) {
    return GET_PROPERTY.apply(properties.get(i));
  }
}
