package net.crate.examples;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AnimalTest {

  @Test
  public void testBasic() throws Exception {
    Animal spiderPig = Animal.builder()
        .name("Spider-Pig")
        .good(false);
    assertThat(spiderPig.name, is("Spider-Pig"));
    assertThat(spiderPig.good, is(false));
  }

  @Test
  public void testStep() throws Exception {
    Animal_Crate.Name name = Animal.builder()
        .name("Spider-Pig");
    assertThat(name.name, is("Spider-Pig"));
  }
}