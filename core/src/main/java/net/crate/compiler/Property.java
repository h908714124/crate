package net.crate.compiler;

import static javax.lang.model.element.Modifier.PRIVATE;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

final class Property extends ParaParameter {

  private final VariableElement variableElement;
  final Model model;

  private Property(VariableElement variableElement, Model model) {
    this.variableElement = variableElement;
    this.model = model;
  }

  static ParaParameter create(
      VariableElement field,
      Model model) {
    Property property = new Property(field, model);
    return Optionalish.create(property).orElse(property);
  }

  TypeName type() {
    return TypeName.get(variableElement.asType());
  }

  TypeMirror asType() {
    return variableElement.asType();
  }

  FieldSpec asField() {
    return FieldSpec.builder(type(),
        name())
        .addModifiers(PRIVATE)
        .build();
  }

  String name() {
    return variableElement.getSimpleName().toString();
  }

  @Override
  <R, P> R accept(Cases<R, P> cases, P p) {
    return cases.property(this, p);
  }
}
