## crate

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/crate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/crate)

When an object is constructed via crate, then each step of the generated builder is a separate object.
This step object is immutable and represents one of the constructor parameters, including all preceding parameters.

#### Basic example

Put the `@Crate` annotation on a class. The class must have a non-private constructor.

````java
@Crate
class Animal {

  final String name;
  final boolean good;

  Animal(String name, boolean good) {
    this.name = name;
    this.good = good;
  }
}
````

A class `Animal_Crate` will be generated in the same package.
You can use the static `Animal_Crate.buider()` method to obtain an instance.

If you'd also like a "normal" builder for updating, you may want to combine this with
[readable](https://github.com/h908714124/readable). 

The following example shows how to add the usual
`builder` and `toBuilder` methods:

#### Example: combining crate and readable

````java
@Readable
@Crate
final class Animal {

  final String name;
  final int numberOfLegs;

  Animal(String name, int numberOfLegs) {
    this.name = name;
    this.numberOfLegs = numberOfLegs;
  }

  static Animal_Crate builder() {
    return Animal_Crate.builder();
  }

  Animal_Builder toBuilder() {
    return Animal_Builder.builder(this);
  }
}
````

If you'd rather have `Animal` implemented by [auto-value](https://github.com/google/auto/tree/master/value),
you need to use the `@AutoCrate` annotation instead.

#### Example: auto-crate

````java
@AutoCrate
@AutoValue
abstract class Animal {

  abstract String name();
  abstract int numberOfLegs();

  static Animal_Crate builder() {
    return Animal_Crate.builder();
  }
}
````

#### Maven Dependency

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>crate</artifactId>
  <version>1.1</version>
  <scope>provided</scope>
</dependency>
````
