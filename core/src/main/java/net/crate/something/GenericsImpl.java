package net.crate.something;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import net.crate.compiler.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.something.GenericsContract.nextStepType;
import static net.crate.something.Util.joinCodeBlocks;
import static net.crate.something.Util.upcase;

final class GenericsImpl {

  private final Model model;

  GenericsImpl(Model model) {
    this.model = model;
  }

  List<TypeSpec> stepImpls(List<List<TypeVariableName>> methodParams,
                           List<List<TypeVariableName>> typeParams) {
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
    ParameterSpec parameter = ParameterSpec.builder(model.properties.get(i).type(),
        model.properties.get(i).name()).build();
    List<FieldSpec> fields = implFields.fields(i);
    TypeSpec.Builder builder = TypeSpec.classBuilder(
        upcase(model.properties.get(i).name()));
    builder.addMethod(createConstructor(fields));
    return builder.addFields(fields)
        .addTypeVariables(typeParams.get(i))
        .addMethod(methodBuilder(model.properties.get(i).name())
            .addParameter(parameter)
            .addTypeVariables(methodParams.get(i))
            .addModifiers(PUBLIC)
            .returns(nextStepType(model, typeParams, i))
            .addCode(getCodeBlock(i, parameter))
            .addExceptions(i == model.properties.size() - 1 ?
                model.thrownTypes :
                emptyList())
            .build())
        .addModifiers(PUBLIC, STATIC, FINAL)
        .build();
  }

  private CodeBlock getCodeBlock(int i, ParameterSpec parameter) {
    if (i == model.properties.size() - 1) {
      return fullInvoke();
    }
    ClassName next = model.generatedClass.nestedClass(
        upcase(model.properties.get(i + 1).name()));
    return CodeBlock.builder()
        .addStatement("return new $T(this, $N)", next, parameter)
        .build();
  }

  private CodeBlock fullInvoke() {
    CodeBlock invoke = basicInvoke();
    return CodeBlock.builder().addStatement("return new $T($L)",
        model.sourceClass, invoke).build();
  }

  private IntFunction<CodeBlock> invokeFn() {
    return i -> {
      CodeBlock.Builder block = CodeBlock.builder();
      for (int j = model.properties.size() - 3; j >= i; j--) {
        String name = model.properties.get(j + 1).name();
        block.add("$L.", name + "Acc");
      }
      String name = model.properties.get(i).name();
      block.add("$L", name);
      return block.build();
    };
  }

  CodeBlock basicInvoke() {
    return IntStream.range(0, model.properties.size())
        .mapToObj(invokeFn())
        .collect(joinCodeBlocks(", "));
  }

  private MethodSpec createConstructor(List<FieldSpec> fields) {
    List<ParameterSpec> parameters = fields.stream().map(
        field -> ParameterSpec.builder(field.type, field.name).build())
        .collect(Collectors.toList());
    return MethodSpec.constructorBuilder()
        .addParameters(parameters)
        .addCode(parameters.stream().map(parameter ->
            CodeBlock.builder()
                .addStatement("this.$N = $N", parameter, parameter)
                .build())
            .collect(joinCodeBlocks("")))
        .addModifiers(PRIVATE)
        .build();
  }
}
