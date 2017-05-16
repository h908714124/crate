package net.crate.examples;

import net.crate.Crate;

@Crate
class Animal {

  private static final Animal_Crate BUILDER =
      Animal_Crate.builder();

  final String name;
  final boolean good;

  Animal(String name, boolean good) {
    this.name = name;
    this.good = good;
  }

  static Animal_Crate builder() {
    return BUILDER;
  }
}
