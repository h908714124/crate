package net.crate.compiler;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class Model {

  private static final Modifier[] PUBLIC_MODIFIER = {PUBLIC};
  private static final Modifier[] NO_MODIFIERS = new Modifier[0];

  private final TypeElement targetClassElement;
  private final TypeElement sourceClassElement;

  final ClassName generatedClass;
  final TypeName targetClass;
  final List<Property> properties;
  final List<TypeName> thrownTypes;
  final VarLife varLife;

  private Model(
      TypeElement sourceClassElement,
      TypeElement targetClassElement,
      ClassName generatedClass,
      List<Property> properties) {
    this.sourceClassElement = sourceClassElement;
    this.targetClassElement = targetClassElement;
    this.generatedClass = generatedClass;
    TypeScanner scanner = TypeScanner.scan(targetClassElement);
    this.properties = scanner.properties;
    this.targetClass = TypeName.get(targetClassElement.asType());
    this.thrownTypes = scanner.constructor.getThrownTypes()
        .stream()
        .map(TypeName::get)
        .collect(toList());
    this.varLife = VarLife.create(
        typevars(targetClassElement),
        stepTypes(properties, targetClass));

  }

  static Optional<Model> create(
      TypeElement sourceClassElement,
      TypeElement targetClassElement,
      ClassName generatedClass) {
    TypeScanner scanner = TypeScanner.scan(targetClassElement);
    if (scanner.properties.isEmpty()) {
      return Optional.empty();
    }
    Model model = new Model(
        sourceClassElement, targetClassElement, generatedClass, scanner.properties);
    return Optional.of(model);
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
      List<Property> properties, TypeName sourceClass) {
    List<TypeName> builder = new ArrayList<>(properties.size() + 1);
    properties.stream().map(Property::type)
        .forEach(builder::add);
    builder.add(sourceClass);
    return builder;
  }

  TypeName sourceClass() {
    return TypeName.get(sourceClassElement.asType());
  }
}
