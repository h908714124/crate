package net.crate.something;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import net.crate.compiler.Model;
import net.zerobuilder.compiler.generate.DtoGoalDetails.AbstractRegularDetails;
import net.zerobuilder.compiler.generate.DtoModule.RegularSimpleModule;
import net.zerobuilder.compiler.generate.DtoModuleOutput.ModuleOutput;
import net.zerobuilder.compiler.generate.DtoRegularGoalDescription.SimpleRegularGoalDescription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.zerobuilder.compiler.generate.DtoGoalDetails.isInstance;
import static net.zerobuilder.compiler.generate.DtoGoalDetails.regularDetailsCases;
import static net.zerobuilder.compiler.generate.ZeroUtil.concat;
import static net.zerobuilder.compiler.generate.ZeroUtil.cons;
import static net.zerobuilder.modules.generics.GenericsContract.stepTypes;

public final class GenericsBuilder {

  private final BiFunction<AbstractRegularDetails, SimpleRegularGoalDescription, List<TypeName>> extendedStepTypes =
      regularDetailsCases(
          (constructor, description) -> stepTypes(description),
          (staticMethod, description) -> stepTypes(description),
          (instanceMethod, description) -> cons(
              description.context.type,
              stepTypes(description)));

  public static List<TypeSpec> process(Model model) {
    AbstractRegularDetails details = description.details;
    List<TypeVariableName> typeParameters = model.typevars();
    VarLife varLife = VarLife.create(
        typeParameters,
        extendedStepTypes.apply(description.details, description),
        isInstance.apply(details));
    GenericsGenerator generator = GenericsGenerator.create(description, varLife);
    return new ModuleOutput(
        generator.builderMethod(description, varLife),
        singletonList(generator.defineImpl()),
        emptyList());
  }
}
