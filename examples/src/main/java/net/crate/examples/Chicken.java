package net.crate.examples;

import net.crate.Crate;

@Crate
class Chicken {

  private static final Chicken_Crate BUILDER =
      Chicken_Crate.builder();

  static Chicken_Crate builder() {
    return BUILDER;
  }
}
