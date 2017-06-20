package net.crate.compiler;

import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

final class TypeScanner {

  final Model model;

  private TypeScanner(Model model) {
    this.model = model;
  }

  static TypeScanner scan(Model model) {
    if (model.sourceClassElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException("The class may not be private",
          model.sourceClassElement);
    }
    if (model.targetClassElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ValidationException("The class may not be abstract",
          model.targetClassElement);
    }
    if (model.sourceClassElement.getEnclosingElement() != null &&
        model.sourceClassElement.getEnclosingElement().getKind() == ElementKind.CLASS &&
        !model.sourceClassElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException("The inner class must be static",
          model.sourceClassElement);
    }
    return new TypeScanner(model);
  }

  List<ParaParameter> properties() {
    return model.constructor().getParameters().stream()
        .map(variableElement -> Property.create(variableElement, model))
        .collect(Collectors.toList());
  }
}
