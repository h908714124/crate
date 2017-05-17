package net.crate.examples;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import net.crate.examples.Toad_Crate.Key;
import org.junit.Test;

public class ToadTest {

  @Test
  public void testToad() throws IOException {
    Key<String> key = Toad_Crate.builder().key("qi");
    Toad<String, Integer> entry = key.value(2);
    assertThat(entry.getKey(), is("qi"));
    assertThat(entry.getValue(), is(2));
  }
}