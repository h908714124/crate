package net.crate.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.FINAL;
import static net.crate.compiler.CrateProcessor.rawType;
import static net.crate.compiler.ParaParameter.GET_PROPERTY;
import static net.crate.compiler.Util.parameterizedTypeName;
import static net.crate.compiler.Util.upcase;

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

  List<FieldSpec> fields(int i) {
    if (i == 0) {
      return emptyList();
    }
    if (i == 1) {
      return singletonList(dataField(i));
    }
    return asList(upField(i), dataField(i));
  }

  MethodSpec constructor(int i) {
    MethodSpec.Builder builder = constructorBuilder();
    List<FieldSpec> fields = fields(i);
    for (FieldSpec field : fields) {
      ParameterSpec p = ParameterSpec.builder(field.type, field.name).build();
      builder.addStatement("this.$N = $N", field, p);
      builder.addParameter(p);
    }
    return builder.addModifiers(model.maybePublic())
        .build();
  }

  private FieldSpec upField(int i) {
    TypeName refType = upType(i);
    return FieldSpec.builder(refType, "up")
        .addModifiers(model.maybePublic())
        .addModifiers(FINAL)
        .build();
  }

  private TypeName upType(int i) {
    return parameterizedTypeName(
        rawType(model.generatedClass)
            .nestedClass(upcase(get(i - 2).name())),
        model.varLife.typeParams.get(i - 2));
  }

  private FieldSpec dataField(int i) {
    return FieldSpec.builder(get(i - 1).type(), get(i - 1).name())
        .addModifiers(model.maybePublic())
        .addModifiers(FINAL)
        .build();
  }

  private Property get(int i) {
    return GET_PROPERTY.apply(properties.get(i));
  }
}
