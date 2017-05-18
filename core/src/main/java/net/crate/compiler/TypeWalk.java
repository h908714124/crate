package net.crate.compiler;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import java.util.Iterator;
import java.util.Stack;

final class TypeWalk implements Iterator<TypeName> {

    private final Stack<TypeName> stack;

    TypeWalk(TypeName type) {
        stack = new Stack<>();
        stack.push(type);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public TypeName next() {
        TypeName type = stack.pop();
        if (type instanceof ParameterizedTypeName) {
            ((ParameterizedTypeName) type).typeArguments.forEach(stack::push);
        }
        if (type instanceof TypeVariableName) {
            ((TypeVariableName) type).bounds.forEach(stack::push);
        }
        return type;
    }
}
