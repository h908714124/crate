package net.crate.examples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.crate.Crate;

@Crate
final class Bird {

  private final ImmutableList<Date> feathers;
  private final ImmutableSet<String> feet;
  private final ImmutableMap<String, String> eyes;

  private final List<Date> beak;
  private final Set<String> wings;
  private final Map<Date, String> tail;

  Bird(ImmutableList<Date> feathers,
       ImmutableSet<String> feet,
       ImmutableMap<String, String> eyes,
       List<Date> beak,
       Set<String> wings,
       Map<Date, String> tail) {
    this.feathers = feathers;
    this.feet = feet;
    this.eyes = eyes;
    this.beak = beak;
    this.wings = wings;
    this.tail = tail;
  }
}
