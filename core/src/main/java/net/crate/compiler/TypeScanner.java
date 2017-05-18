package net.crate.compiler;

import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import net.crate.Crate;

final class TypeScanner {

  final List<Property> properties;
  final ExecutableElement constructor;

  private TypeScanner(List<Property> properties, ExecutableElement constructor) {
    this.properties = properties;
    this.constructor = constructor;
  }

  static TypeScanner scan(TypeElement sourceClassElement) {
    if (sourceClassElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException("The class may not be private", sourceClassElement);
    }
    if (sourceClassElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ValidationException("The class may not be abstract", sourceClassElement);
    }
    if (sourceClassElement.getEnclosingElement() != null &&
        sourceClassElement.getEnclosingElement().getKind() == ElementKind.CLASS &&
        !sourceClassElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException("The inner class must be static", sourceClassElement);
    }
    ExecutableElement constructor = constructor(sourceClassElement);
    List<Property> properties = constructor.getParameters().stream()
        .map(Property::create)
        .collect(Collectors.toList());
    return new TypeScanner(properties, constructor);
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
        .filter(constructor -> constructor.getAnnotation(Crate.Constructor.class) != null)
        .collect(Collectors.toList());
    if (constructors.isEmpty()) {
      throw new ValidationException("Use @Crate.Constructor " +
          "to mark a constructor", sourceClassElement);
    }
    if (constructors.size() > 1) {
      throw new ValidationException("Only one @Crate.Constructor " +
          "annotation is allowed per class", sourceClassElement);
    }
    return constructors.get(0);
  }
}
