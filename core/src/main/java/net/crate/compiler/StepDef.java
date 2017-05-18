package net.crate.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.List;

final class StepDef {

  private final TypeSpec typeSpec;
  private final List<MethodSpec> nextMethod;

  StepDef(TypeSpec typeSpec, List<MethodSpec> nextMethod) {
    this.typeSpec = typeSpec;
    this.nextMethod = nextMethod;
  }

  TypeSpec typeSpec() {
    return typeSpec;
  }

  List<MethodSpec> nextMethods() {
    return nextMethod;
  }
}
