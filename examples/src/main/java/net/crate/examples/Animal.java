package net.crate.examples;

import net.crate.Crate;

@Crate
class Animal {

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
    return Animal_Crate.builder();
  }
}
