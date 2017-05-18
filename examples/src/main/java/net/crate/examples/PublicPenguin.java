package net.crate.examples;

import com.google.auto.value.AutoValue;
import net.crate.AutoCrate;

@AutoValue
@AutoCrate
public abstract class PublicPenguin {

  public abstract String foo();
  public abstract String bar();
}
