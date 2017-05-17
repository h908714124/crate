package net.crate.examples;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AnacondaTest {

  @Test
  public void testBasic() throws Exception {
    Anaconda spiderPig = Anaconda.builder()
        .name("Spider-Pig")
        .good(true);
    assertThat(spiderPig.name(), is("Spider-Pig"));
    assertThat(spiderPig.good(), is(true));
  }
}