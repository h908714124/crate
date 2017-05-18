package net.crate.compiler;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.crate.compiler.CrateProcessor.rawType;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

import com.squareup.javapoet.MethodSpec;
import java.util.List;

final class ImplFields {

  private final Model model;

  private ImplFields(
      Model model) {
    this.model = model;
  }

  static ImplFields create(
      Model model) {
    return new ImplFields(model);
  }

  List<MethodSpec> fields(int i) {
    return i == 0 ?
        emptyList() :
        i == 1 ?
            singletonList(data(i)) :
            asList(ref(i), data(i));
  }

  private MethodSpec ref(int i) {
    String returnType = upcase(model.properties.get(i - 2).name());
    return methodBuilder("ref")
        .returns(parameterizedTypeName(
            rawType(model.generatedClass)
                .nestedClass(returnType),
            model.varLife.typeParams.get(i - 2)))
        .addModifiers(ABSTRACT)
        .addModifiers(model.maybePublic())
        .build();
  }

  private MethodSpec data(int i) {
    return methodBuilder("get")
        .returns(model.properties.get(i - 1).type())
        .addModifiers(ABSTRACT)
        .addModifiers(model.maybePublic())
        .build();
  }
}
