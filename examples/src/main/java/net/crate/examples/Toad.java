package net.crate.examples;


import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import net.crate.Crate;

@Crate
final class Toad<K, V> extends SimpleEntry<K, V> {

  Toad(K key, V value) throws IOException {
    super(key, value);
  }
}