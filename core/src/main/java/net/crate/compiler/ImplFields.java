package net.crate.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.crate.compiler.CrateProcessor.rawType;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

final class ImplFields {

  private final Model model;
  private final List<List<TypeVariableName>> typeParams;

  ImplFields(Model model,
             List<List<TypeVariableName>> typeParams) {
    this.model = model;
    this.typeParams = typeParams;
  }

  List<MethodSpec> fields(int i) {
    return i == 0 ?
        emptyList() :
        i == 1 ?
            singletonList(data(i)) :
            asList(pointer(i), data(i));
  }

  private MethodSpec pointer(int i) {
    return methodBuilder(
        "pointer")
        .returns(parameterizedTypeName(
            rawType(model.generatedClass)
                .nestedClass(upcase(model.properties.get(i - 1).name())),
            typeParams.get(i - 1)))
        .addModifiers(ABSTRACT)
        .build();
  }

  private MethodSpec data(int i) {
    return methodBuilder("data")
        .returns(model.properties.get(i - 1).type())
        .addModifiers(ABSTRACT)
        .build();
  }
}
