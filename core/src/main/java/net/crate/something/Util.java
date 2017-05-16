package net.crate.something;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;
import static java.util.Collections.emptySet;

final class Util {


  private static final CodeBlock emptyCodeBlock = CodeBlock.of("");

  static <P> List<P> cons(P first, List<? extends P> list) {
    List<P> builder = new ArrayList<>(list.size() + 1);
    builder.add(first);
    builder.addAll(list);
    return builder;
  }

  static boolean references(TypeName type, TypeVariableName test) {
    if (!maybeTypevars(type)) {
      return false;
    }
    if (type instanceof TypeVariableName
        && ((TypeVariableName) type).bounds.isEmpty()) {
      return type.equals(test);
    }
    TypeWalk walk = new TypeWalk(type);
    while (walk.hasNext()) {
      if (walk.next().equals(test)) {
        return true;
      }
    }
    return false;
  }

  private static boolean maybeTypevars(TypeName type) {
    if (!(type instanceof ParameterizedTypeName
        || type instanceof TypeVariableName)) {
      return false;
    }
    if (type instanceof ParameterizedTypeName) {
      for (TypeName targ : ((ParameterizedTypeName) type).typeArguments) {
        if (targ instanceof ParameterizedTypeName
            || targ instanceof TypeVariableName) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  static <E> List<E> reverse(List<E> in) {
    List<E> builder = new ArrayList<>(in);
    Collections.reverse(builder);
    return builder;
  }

  static TypeName parameterizedTypeName(ClassName raw, List<TypeVariableName> typeVars) {
    if (typeVars.isEmpty()) {
      return raw;
    }
    return ParameterizedTypeName.get(raw,
        typeVars.toArray(new TypeVariableName[typeVars.size()]));
  }

  public static String upcase(String s) {
    if (s.isEmpty() || isUpperCase(s.charAt(0))) {
      return s;
    }
    return toUpperCase(s.charAt(0)) + s.substring(1);
  }
  
  static Collector<CodeBlock, List<CodeBlock>, CodeBlock> joinCodeBlocks(String delimiter) {
    return new Collector<CodeBlock, List<CodeBlock>, CodeBlock>() {
      @Override
      public Supplier<List<CodeBlock>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<CodeBlock>, CodeBlock> accumulator() {
        return List::add;
      }

      @Override
      public BinaryOperator<List<CodeBlock>> combiner() {
        return (left, right) -> {
          left.addAll(right);
          return left;
        };
      }

      @Override
      public Function<List<CodeBlock>, CodeBlock> finisher() {
        return blocks -> {
          if (blocks.isEmpty()) {
            return emptyCodeBlock;
          }
          CodeBlock.Builder builder = CodeBlock.builder();
          for (int i = 0; i < blocks.size() - 1; i++) {
            builder.add(blocks.get(i));
            if (!delimiter.isEmpty()) {
              builder.add(delimiter);
            }
          }
          builder.add(blocks.get(blocks.size() - 1));
          return builder.build();
        };
      }

      @Override
      public Set<Characteristics> characteristics() {
        return emptySet();
      }
    };
  }

}
