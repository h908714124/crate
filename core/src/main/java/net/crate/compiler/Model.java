package net.crate.compiler;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.crate.compiler.LessTypes.asType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

final class Model {

  private static final Modifier[] PUBLIC_MODIFIER = {PUBLIC};
  private static final Modifier[] NO_MODIFIERS = new Modifier[0];

  final TypeElement targetClassElement;
  final TypeElement sourceClassElement;

  final ClassName generatedClass;
  final TypeName targetClass;
  final List<TypeName> thrownTypes;
  final VarLife varLife;
  final ExecutableElement constructor;

  private Model(
      TypeElement sourceClassElement,
      TypeElement targetClassElement,
      ClassName generatedClass,
      ExecutableElement constructor) {
    this.sourceClassElement = sourceClassElement;
    this.targetClassElement = targetClassElement;
    this.generatedClass = generatedClass;
    this.targetClass = TypeName.get(targetClassElement.asType());
    this.thrownTypes = constructor.getThrownTypes()
        .stream()
        .map(TypeName::get)
        .collect(toList());
    this.varLife = VarLife.create(
        typevars(targetClassElement),
        stepTypes(constructor.getParameters(), targetClass));
    this.constructor = constructor;
  }

  static TypeScanner create(
      TypeElement sourceClassElement,
      TypeElement targetClassElement,
      ClassName generatedClass) {
    ExecutableElement constructor = constructor(targetClassElement);
    Model model = new Model(
        sourceClassElement,
        targetClassElement,
        generatedClass,
        constructor);
    return TypeScanner.scan(model);
  }

  private boolean isPublic() {
    return sourceClassElement.getModifiers().contains(PUBLIC);
  }

  Modifier[] maybePublic() {
    if (isPublic()) {
      return PUBLIC_MODIFIER;
    }
    return NO_MODIFIERS;
  }

  private static List<TypeVariableName> typevars(TypeElement sourceClassElement) {
    return sourceClassElement.getTypeParameters().stream()
        .map(TypeVariableName::get)
        .collect(toList());
  }

  private static List<TypeName> stepTypes(
      List<? extends VariableElement> properties,
      TypeName sourceClass) {
    List<TypeName> builder = new ArrayList<>(properties.size() + 1);
    properties.stream()
        .map(VariableElement::asType)
        .map(TypeName::get)
        .forEach(builder::add);
    builder.add(sourceClass);
    return builder;
  }


  ExecutableElement constructor() {
    return constructor(targetClassElement);
  }

  private static ExecutableElement constructor(TypeElement sourceClassElement) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(sourceClassElement.getEnclosedElements())
            .stream()
            .filter(constructor -> !constructor.getModifiers().contains(Modifier.PRIVATE))
            .collect(Collectors.toList());
    if (constructors.size() == 1) {
      return constructors.get(0);
    }
    if (constructors.isEmpty()) {
      throw new ValidationException("No non-private constructor found", sourceClassElement);
    }
    constructors = constructors.stream()
        .filter(constructor ->
            constructor.getAnnotationMirrors().stream().anyMatch(mirror ->
                asType(mirror.getAnnotationType().asElement())
                    .getQualifiedName().toString()
                    .endsWith(".Constructor")))
        .collect(Collectors.toList());
    if (constructors.isEmpty()) {
      throw new ValidationException("Use @Crate.Constructor " +
          "to tag a constructor", sourceClassElement);
    }
    if (constructors.size() > 1) {
      throw new ValidationException("Only one @Constructor " +
          "annotation is allowed per class", sourceClassElement);
    }
    return constructors.get(0);
  }

  TypeName sourceClass() {
    return TypeName.get(sourceClassElement.asType());
  }
}
