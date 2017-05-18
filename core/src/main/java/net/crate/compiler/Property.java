package net.crate.compiler;

import com.squareup.javapoet.TypeName;
import java.util.Optional;
import javax.lang.model.element.VariableElement;

final class Property {

  private final VariableElement field;

  private Property(VariableElement field) {
    this.field = field;
  }

  static Property create(VariableElement field) {
    return new Property(field);
  }

  TypeName type() {
    return TypeName.get(field.asType());
  }

  String name() {
    return field.getSimpleName().toString();
  }

  Optional<Optionalish> optionalish() {
    return Optionalish.create(type());
  }
}
