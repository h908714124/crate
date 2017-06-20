package net.crate.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

abstract class ParaParameter {

  static abstract class Cases<R, P> {

    abstract R property(Property property, P p);

    abstract R optionalish(Optionalish optionalish, P p);
  }

  static <R> Function<ParaParameter, R> asFunction(Cases<R, Void> cases) {
    return parameter -> parameter.accept(cases, null);
  }

  static <R, P> BiFunction<ParaParameter, P, R> asBiFunction(Cases<R, P> cases) {
    return (parameter, p) -> parameter.accept(cases, p);
  }

  private static <P> BiConsumer<ParaParameter, P> asConsumer(Cases<Void, P> cases) {
    return (parameter, p) -> parameter.accept(cases, p);
  }

  abstract <R, P> R accept(Cases<R, P> cases, P p);

  static final Function<ParaParameter, Property> GET_PROPERTY =
      asFunction(new Cases<Property, Void>() {
        @Override
        Property property(Property property, Void _null) {
          return property;
        }

        @Override
        Property optionalish(Optionalish optionalish, Void _null) {
          return optionalish.property;
        }
      });

  static final Function<ParaParameter, ParameterSpec> AS_SETTER_PARAMETER =
      asFunction(new Cases<ParameterSpec, Void>() {
        @Override
        ParameterSpec property(Property property, Void _null) {
          return ParameterSpec.builder(property.type(), property.name()).build();
        }

        @Override
        ParameterSpec optionalish(Optionalish optionalish, Void _null) {
          return ParameterSpec.builder(optionalish.property.type(),
              optionalish.property.name()).build();
        }
      });

  static final Function<ParaParameter, CodeBlock> SETTER_ASSIGNMENT =
      asFunction(new ParaParameter.Cases<CodeBlock, Void>() {
        @Override
        CodeBlock property(Property property, Void _null) {
          FieldSpec field = property.asField();
          ParameterSpec p = AS_SETTER_PARAMETER.apply(property);
          return CodeBlock.builder()
              .addStatement("this.$N = $N", field, p).build();
        }

        @Override
        CodeBlock optionalish(Optionalish optionalish, Void _null) {
          FieldSpec field = optionalish.property.asField();
          ParameterSpec p = AS_SETTER_PARAMETER.apply(optionalish);
          return CodeBlock.builder()
              .addStatement("this.$N = $N", field, p).build();
        }
      });

  static final BiConsumer<ParaParameter, TypeSpec.Builder> ADD_ACCUMULATOR_FIELD =
      asConsumer(new ParaParameter.Cases<Void, TypeSpec.Builder>() {
        @Override
        Void property(Property property, TypeSpec.Builder builder) {
          return null;
        }

        @Override
        Void optionalish(Optionalish optionalish, TypeSpec.Builder builder) {
          return null;
        }
      });

  static final BiConsumer<ParaParameter, TypeSpec.Builder> ADD_ACCUMULATOR_METHOD =
      asConsumer(new ParaParameter.Cases<Void, TypeSpec.Builder>() {
        @Override
        Void property(Property property, TypeSpec.Builder builder) {
          return null;
        }

        @Override
        Void optionalish(Optionalish optionalish, TypeSpec.Builder builder) {
          return null;
        }
      });

  static final BiConsumer<ParaParameter, TypeSpec.Builder> ADD_ACCUMULATOR_OVERLOAD =
      asConsumer(new Cases<Void, TypeSpec.Builder>() {
        @Override
        Void property(Property property, TypeSpec.Builder builder) {
          return null;
        }

        @Override
        Void optionalish(Optionalish optionalish, TypeSpec.Builder builder) {
          return null;
        }
      });

  static final BiConsumer<ParaParameter, CodeBlock.Builder> CLEAR_ACCUMULATOR =
      asConsumer(new ParaParameter.Cases<Void, CodeBlock.Builder>() {
        @Override
        Void property(Property property, CodeBlock.Builder builder) {
          return null;
        }

        @Override
        Void optionalish(Optionalish optionalish, CodeBlock.Builder builder) {
          return null;
        }
      });


  static final BiConsumer<ParaParameter, TypeSpec.Builder> ADD_OPTIONALISH_OVERLOAD =
      asConsumer(new Cases<Void, TypeSpec.Builder>() {
        @Override
        Void property(Property parameter, TypeSpec.Builder builder) {
          return null;
        }

        @Override
        Void optionalish(Optionalish optionalish, TypeSpec.Builder builder) {
          builder.addMethod(optionalish.convenienceOverloadMethod());
          return null;
        }
      });
}