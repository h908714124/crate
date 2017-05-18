package net.crate.examples;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.junit.Test;

public class StarfishTest {

  @Test
  public void testBuilder() throws Exception {
    Starfish starfish = Starfish.builder()
        .arms(singletonList("a"))
        .legs(singletonList(1));
    assertThat(starfish.arms(), is(Optional.of(singletonList("a"))));
    assertThat(starfish.legs(), is(Optional.of(singletonList(1))));
  }
}