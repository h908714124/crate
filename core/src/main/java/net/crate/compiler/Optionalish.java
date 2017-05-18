package net.crate.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

final class Optionalish {

  private static final ClassName OPTIONAL_CLASS =
      ClassName.get(Optional.class);

  private static final List<Optionalish> OPTIONAL_PRIMITIVES =
      Arrays.asList(
          new Optionalish(ClassName.get(OptionalInt.class), TypeName.INT),
          new Optionalish(ClassName.get(OptionalDouble.class), TypeName.DOUBLE),
          new Optionalish(ClassName.get(OptionalLong.class), TypeName.LONG));

  final ClassName wrapper;
  final TypeName wrapped;

  private Optionalish(ClassName wrapper, TypeName wrapped) {
    this.wrapper = wrapper;
    this.wrapped = wrapped;
  }

  static Optional<Optionalish> create(TypeName typeName) {
    if (typeName instanceof ClassName) {
      for (Optionalish optionalPrimitive : OPTIONAL_PRIMITIVES) {
        if (optionalPrimitive.wrapper.equals(typeName)) {
          return Optional.of(optionalPrimitive);
        }
      }
      return Optional.empty();
    }
    if (!(typeName instanceof ParameterizedTypeName)) {
      return Optional.empty();
    }
    ParameterizedTypeName type = (ParameterizedTypeName) typeName;
    if (!type.rawType.equals(OPTIONAL_CLASS)) {
      return Optional.empty();
    }
    Optionalish optionalish = new Optionalish(OPTIONAL_CLASS,
        type.typeArguments.get(0));
    return Optional.of(optionalish);
  }

  boolean isOptional() {
    return wrapper.equals(OPTIONAL_CLASS);
  }

  String ofLiteral() {
    return isOptional() ? "ofNullable" : "of";
  }
}
