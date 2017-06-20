package net.crate.compiler;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.crate.compiler.CrateProcessor.rawType;
import static net.crate.compiler.ParaParameter.GET_PROPERTY;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

import com.squareup.javapoet.MethodSpec;
import java.util.List;

final class ImplFields {

  private final Model model;
  private final List<ParaParameter> properties;

  private ImplFields(
      Model model,
      List<ParaParameter> properties) {
    this.model = model;
    this.properties = properties;
  }

  static ImplFields create(
      Model model,
      List<ParaParameter> properties) {
    return new ImplFields(model, properties);
  }

  List<MethodSpec> fields(int i) {
    if (i == 0) {
      return emptyList();
    }
    if (i == 1) {
      return singletonList(data(i));
    }
    return asList(ref(i), data(i));
  }

  private MethodSpec ref(int i) {
    String returnType = upcase(get(i - 2).name());
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
        .returns(get(i - 1).type())
        .addModifiers(ABSTRACT)
        .addModifiers(model.maybePublic())
        .build();
  }

  private Property get(int i) {
    return GET_PROPERTY.apply(properties.get(i));
  }
}
