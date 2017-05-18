package net.crate.examples;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Optional;
import net.crate.AutoCrate;

@AutoValue
@AutoCrate
abstract class Starfish {

  abstract Optional<List<String>> arms();

  abstract Optional<List<Integer>> legs();

  static Starfish_Crate builder() {
    return Starfish_Crate.builder();
  }
}
