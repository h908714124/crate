package net.crate.compiler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.crate.compiler.Util.references;
import static net.crate.compiler.Util.reverse;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class VarLife {

  final List<List<TypeVariableName>> typeParams;
  final List<List<TypeVariableName>> methodParams;

  private VarLife(
      List<List<TypeVariableName>> typeParams,
      List<List<TypeVariableName>> methodParams) {
    this.typeParams = typeParams;
    this.methodParams = methodParams;
  }

  /**
   * @param typeParameters contextual type variables
   * @param steps          parameter types, followed by return type
   * @return helper object
   */
  static VarLife create(
      List<TypeVariableName> typeParameters,
      List<TypeName> steps) {
    if (steps.size() == 0) {
      return new VarLife(
          emptyList(),
          emptyList());
    }
    if (steps.size() == 1) {
      return new VarLife(
          singletonList(emptyList()),
          singletonList(emptyList()));
    }
    return new VarLife(
        typeParams(steps, typeParameters),
        methodParams(steps, typeParameters));
  }

  private static final Supplier<Stream<List<TypeVariableName>>> emptyLists =
      () -> Stream.generate(ArrayList::new);

  private static List<List<TypeVariableName>> emptyLists(int n) {
    List<List<TypeVariableName>> builder = new ArrayList<>(n);
    emptyLists.get().limit(n).forEach(builder::add);
    return builder;
  }

  private static List<List<TypeVariableName>> methodParams(
      List<TypeName> steps,
      List<TypeVariableName> typeParameters) {
    List<List<TypeVariableName>> varLifes = varLifes(steps, typeParameters);
    List<List<TypeVariableName>> builder = emptyLists(varLifes.size() - 1);
    builder.get(0).addAll(varLifes.get(0));
    for (int i = 1; i < varLifes.size() - 1; i++) {
      for (TypeVariableName t : varLifes.get(i)) {
        if (!varLifes.get(i - 1).contains(t)) {
          builder.get(i).add(t);
        }
      }
    }
    return builder;
  }

  private static List<List<TypeVariableName>> typeParams(
      List<TypeName> steps,
      List<TypeVariableName> typeParameters) {
    List<List<TypeVariableName>> varLifes = accLife(steps, typeParameters);
    return varLifes.subList(0, varLifes.size() - 2);
  }

  private static List<List<TypeVariableName>> varLifes(
      List<TypeName> steps,
      List<TypeVariableName> typeParameters) {
    List<List<TypeVariableName>> inc = accLife(steps, typeParameters);
    List<List<TypeVariableName>> dec = reverse(
        accLife(reverse(steps), typeParameters));
    List<List<TypeVariableName>> builder = emptyLists(steps.size());
    for (int i = 0; i < builder.size(); i++) {
      for (TypeVariableName t : typeParameters) {
        if (inc.get(i).contains(t) && dec.get(i).contains(t)) {
          builder.get(i).add(t);
        }
      }
    }
    return builder;
  }

  private static List<List<TypeVariableName>> accLife(
      List<TypeName> steps,
      List<TypeVariableName> typeParameters) {
    List<List<TypeVariableName>> builder = emptyLists(steps.size());
    for (TypeVariableName typeParameter : typeParameters) {
      int start = varLifeStart(typeParameter, steps);
      if (start >= 0) {
        for (int i = start; i < steps.size(); i++) {
          builder.get(i).add(typeParameter);
        }
      }
    }
    return builder;
  }

  private static int varLifeStart(
      TypeVariableName typeParameter,
      List<TypeName> steps) {
    for (int i = 0; i < steps.size(); i++) {
      TypeName step = steps.get(i);
      if (references(step, typeParameter)) {
        return i;
      }
    }
    return -1;
  }
}
