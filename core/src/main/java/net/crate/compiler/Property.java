package net.crate.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;

public final class Property {

  private final VariableElement field;

  private Property(VariableElement field) {
    this.field = field;
  }

  static Property create(VariableElement field) {
    return new Property(field);
  }

  public TypeName type() {
    return TypeName.get(field.asType());
  }

  public String name() {
    return field.getSimpleName().toString();
  }

  public FieldSpec.Builder asField() {
    return FieldSpec.builder(type(),
        name()).addModifiers(PRIVATE, FINAL);
  }
}
