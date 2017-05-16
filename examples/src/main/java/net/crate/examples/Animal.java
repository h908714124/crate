package net.crate.examples;

import net.crate.Crate;

import java.util.Optional;

@Crate
class Animal<E> {

  final String name;
  final boolean good;
  final int legs;
  final Optional<E> friend;
  final E harp;

  Animal(
      Optional<E> friend,
      String name,
      boolean good,
      int legs,
      E harp) {
    this.name = name;
    this.good = good;
    this.legs = legs;
    this.friend = friend;
    this.harp = harp;
  }
}
