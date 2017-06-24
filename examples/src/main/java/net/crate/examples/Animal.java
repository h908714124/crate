package net.crate.examples;

import net.crate.Crate;

@Crate
class Animal {

  final String name;
  final boolean good;

  Animal(String name, boolean good) {
    this.name = name;
    this.good = good;
  }

  static Animal_Crate builder() {
    return Animal_Crate.builder();
  }
}
