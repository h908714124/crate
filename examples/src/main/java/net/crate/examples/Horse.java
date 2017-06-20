package net.crate.examples;

import net.crate.Crate;

@Crate
class Horse {

  final String name;

  Horse(String name) {
    this.name = name;
  }

  static Horse_Crate builder() {
    return Horse_Crate.builder();
  }
}
