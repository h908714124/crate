package net.crate.something;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import net.crate.compiler.Model;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.crate.compiler.CrateProcessor.rawType;
import static net.crate.something.Util.joinCodeBlocks;
import static net.crate.something.Util.parameterizedTypeName;
import static net.crate.something.Util.upcase;

final class ImplFields {

  private final Model model;
  private final List<List<TypeVariableName>> typeParams;

  ImplFields(Model model,
             List<List<TypeVariableName>> typeParams) {
    this.model = model;
    this.typeParams = typeParams;
  }

  List<FieldSpec> fields(int i) {
    return i == 0 ?
        emptyList() :
        normalFields(i);
  }

  private List<FieldSpec> normalFields(int i) {
    TypeName implType = parameterizedTypeName(
        rawType(model.generatedClass)
            .nestedClass(upcase(model.properties.get(i - 1).name())),
        typeParams.get(i - 1));
    return asList(
        FieldSpec.builder(implType,
            model.properties.get(i - 1).name() + "Acc",
            PRIVATE, FINAL).build(),
        FieldSpec.builder(model.properties.get(i - 1).type(),
            model.properties.get(i - 1).name(),
            PRIVATE, FINAL).build());
  }
}
