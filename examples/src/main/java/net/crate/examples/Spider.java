package net.crate.examples;

import com.google.auto.value.AutoValue;
import java.util.Map;
import java.util.Optional;
import net.crate.AutoCrate;

@AutoCrate
@AutoValue
abstract class Spider<A, B extends A> {

  abstract A eyes();

  abstract Optional<A> hair();

  abstract A legs();

  abstract B teeth();

  abstract A feet();

  abstract A hands();

  static Spider_Crate builder() {
    return Spider_Crate.builder();
  }

  @AutoCrate
  @AutoValue
  static abstract class Ostrich<A, B extends A> {

    abstract Optional<Spider<A, B>> spider();

    abstract Map<B, A> legs();

    abstract Map<B, A> wings();

    abstract Optional<Map<Map<B, A>, B>> feet();

    abstract Map<Map<B, A>, B> toes();

    static Spider_Ostrich_Crate builder() {
      return Spider_Ostrich_Crate.builder();
    }
  }
}
