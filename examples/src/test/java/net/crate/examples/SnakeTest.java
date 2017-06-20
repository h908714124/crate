package net.crate.examples;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SnakeTest {

  @Test
  public void eye() throws Exception {
    Snake snake = Snake.builder().name("python");
    assertThat(snake.name(), is("python"));
  }
}