package net.crate.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.crate.compiler.CrateProcessor.rawType;

final class Model {

  private static final String SUFFIX = "_Crate";
  private static final Modifier[] PUBLIC_MODIFIER = {PUBLIC};
  private static final Modifier[] NO_MODIFIERS = new Modifier[0];

  private final TypeElement sourceClassElement;

  final ClassName generatedClass;
  final TypeName sourceClass;
  final List<Property> properties;
  final List<TypeName> thrownTypes;

  private Model(ClassName generatedClass,
                TypeElement sourceClassElement) {
    this.generatedClass = generatedClass;
    this.sourceClassElement = sourceClassElement;
    TypeScanner scanner = TypeScanner.scan(sourceClassElement);
    this.properties = scanner.properties;
    this.sourceClass = TypeName.get(sourceClassElement.asType());
    this.thrownTypes = scanner.constructor.getThrownTypes()
        .stream()
        .map(TypeName::get)
        .collect(toList());
  }

  static Model create(TypeElement sourceClassElement) {
    TypeName sourceClass = TypeName.get(sourceClassElement.asType());
    ClassName generatedClass = peer(sourceClass);
    return new Model(generatedClass, sourceClassElement);
  }

  private static ClassName peer(TypeName type) {
    String name = String.join("_", rawType(type).simpleNames()) + SUFFIX;
    return rawType(type).topLevelClassName().peerClass(name);
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

  public List<TypeVariableName> typevars() {
    return sourceClassElement.getTypeParameters().stream()
        .map(TypeVariableName::get)
        .collect(toList());
  }

  private static List<TypeName> typeArguments(TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) typeName).typeArguments;
    }
    return Collections.emptyList();
  }

  String cacheWarning() {
    return "Caching not implemented: " +
        rawType(sourceClass).simpleName() +
        "<" +
        typevars().stream()
            .map(TypeVariableName::toString)
            .collect(joining(", ")) +
        "> has type parameters";
  }
}
