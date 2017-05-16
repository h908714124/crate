package net.crate.examples;

import net.crate.Crate;

@Crate
class Animal {

  private static final ThreadLocal<Animal_Builder.PerThreadFactory> FACTORY =
      ThreadLocal.withInitial(Animal_Builder::perThreadFactory);

  final String name;
  final boolean good;

  Animal(String name, boolean good) {
    this.name = name;
    this.good = good;
  }

  Animal_Builder toBuilder() {
    return FACTORY.get().builder(this);
  }
}