package net.crate.something;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

final class ConstructorGoalDetails {

  final TypeName goalType;
  final List<TypeVariableName> instanceTypeParameters;
  final String name;

  /**
   * parameter names in original order
   */
  final List<String> parameterNames;

  private ConstructorGoalDetails(ClassName goalType, String name, List<String> parameterNames,
                                 List<TypeVariableName> instanceTypeParameters) {
    this.name = name;
    this.parameterNames = parameterNames;
    this.goalType = parameterizedTypeName(goalType, instanceTypeParameters);
    this.instanceTypeParameters = instanceTypeParameters;
  }

  public TypeName type() {
    return goalType;
  }
}
