package net.crate.examples;

import com.google.auto.value.AutoValue;
import net.crate.AutoCrate;

@AutoValue
@AutoCrate
abstract class Anaconda {

  abstract String name();

  abstract boolean good();

  static Anaconda_Crate builder() {
    return Anaconda_Crate.builder();
  }
}
