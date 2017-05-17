package net.crate.examples;

import com.google.auto.value.AutoValue;
import net.crate.AutoCrate;

@AutoValue
@AutoCrate
abstract class Anaconda {

  private static final Anaconda_Crate BUILDER =
      Anaconda_Crate.builder();

  abstract String name();

  abstract boolean good();

  static Anaconda_Crate builder() {
    return BUILDER;
  }
}
