package net.crate.examples;

import com.google.auto.value.AutoValue;
import net.crate.AutoCrate;

@AutoValue
@AutoCrate
abstract class Snake {

  abstract String name();

  static Snake_Crate builder() {
    return Snake_Crate.builder();
  }
}
