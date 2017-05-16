package net.crate.examples;

import net.crate.Crate;

@Crate
class Horse {

  private static final Horse_Crate BUILDER =
      Horse_Crate.builder();

  final String name;

  Horse(String name) {
    this.name = name;
  }

  static Horse_Crate builder() {
    return BUILDER;
  }
}
