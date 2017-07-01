package net.crate.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;
import static net.crate.compiler.ParaParameter.GET_PROPERTY;
import static net.crate.compiler.ParaParameter.asBiFunction;
import static net.crate.compiler.Util.upcase;

final class GenericsImpl {

  private final Model model;
  private final List<ParaParameter> properties;
  private final BiFunction<ParaParameter, Integer, ConvenienceNext.NextResult> convenienceNext;

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
    ConvenienceNext.NextResult firstStep = convenienceNext.apply(properties.get(0), 0);
    return new StepDefs(firstStep.methods,
        firstStep.extraTypes, builder);
  }

  static StepDefs stepImpls(
      Model model,
      List<ParaParameter> properties) {
    return new GenericsImpl(model, properties).stepImpls();
  }

  private TypeSpec stepDef(
      ImplFields implFields,
      int i) {
    ConvenienceNext.NextResult nextResult = convenienceNext.apply(properties.get(i), i);
    return TypeSpec.classBuilder(
        upcase(get(i - 1).name()))
        .addFields(implFields.fields(i))
        .addTypeVariables(model.varLife.typeParams.get(i - 1))
        .addMethods(nextResult.methods)
        .addTypes(nextResult.extraTypes)
        .addMethod(implFields.constructor(i))
        .addModifiers(STATIC, FINAL)
        .addModifiers(model.maybePublic())
        .build();
  }

  static final class StepDefs {
    final List<MethodSpec> initMethods;
    final List<TypeSpec> initExtraTypes;
    final List<TypeSpec> steps;
    private StepDefs(
        List<MethodSpec> initMethods,
        List<TypeSpec> initExtraTypes,
        List<TypeSpec> steps) {
      this.initMethods = initMethods;
      this.initExtraTypes = initExtraTypes;
      this.steps = steps;
    }
  }

  private Property get(int i) {
    return GET_PROPERTY.apply(properties.get(i));
  }
}
