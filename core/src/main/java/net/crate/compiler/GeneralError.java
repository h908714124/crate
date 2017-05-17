package net.crate.compiler;

import javax.lang.model.element.TypeElement;

final class GeneralError extends Exception {

  final TypeElement context;

  GeneralError(Exception exception, TypeElement context) {
    super(exception);
    this.context = context;
  }
}
