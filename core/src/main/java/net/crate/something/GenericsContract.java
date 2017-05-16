package net.crate.something;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import net.crate.compiler.Model;

import java.util.ArrayList;
import java.util.List;

import static net.crate.something.Util.parameterizedTypeName;
import static net.crate.something.Util.upcase;

final class GenericsContract {

  static TypeName nextStepType(
      Model model,
      List<List<TypeVariableName>> typeParams,
      int i) {
    if (i == model.properties.size() - 1) {
      return model.sourceClass;
    }
    ClassName rawNext = model.generatedClass
        .nestedClass(upcase(
            model.properties.get(i + 1).name()));
    return parameterizedTypeName(rawNext, typeParams.get(i + 1));
  }

  static List<TypeName> stepTypes(Model model) {
    List<TypeName> builder = new ArrayList<>(model.properties.size() + 1);
    model.properties.stream().map(step -> step.type())
        .forEach(builder::add);
    builder.add(model.sourceClass);
    return builder;
  }
}