package net.crate.compiler;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.crate.compiler.CrateProcessor.rawType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class Model {

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

  static Model create(TypeElement sourceClassElement, ClassName generatedClass) {
    return new Model(generatedClass, sourceClassElement);
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

  List<TypeVariableName> typevars() {
    return sourceClassElement.getTypeParameters().stream()
        .map(TypeVariableName::get)
        .collect(toList());
  }
}
