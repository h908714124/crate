package net.crate.examples;

import net.crate.Crate;

@Crate
class Animal {

  private static final Animal_Crate BUILDER =
      Animal_Crate.builder();

  final String name;
  final boolean good;

  @Crate.Constructor
  Animal(String name, boolean good) {
    this.name = name;
    this.good = good;
  }

  Animal(boolean good) {
    this("Charlie", good);
  }

  static Animal_Crate builder() {
    return BUILDER;
  }
}
