package net.crate.examples;

import com.google.auto.value.AutoValue;
import net.crate.AutoCrate;

@AutoValue
@AutoCrate
abstract class PackagePiranha {

  abstract String foo();

  abstract String bar();
}
