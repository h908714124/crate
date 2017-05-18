package net.crate.examples;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Modifier;
import org.junit.Test;

public class PublicPenguinTest {

  @Test
  public void testCrate() throws Exception {
    String classModifiers = Modifier.toString(
        PublicPenguin_Crate.class.getModifiers());
    assertThat(classModifiers, containsString("public"));
    assertThat(classModifiers, containsString("final"));
    String builderMethodModifiers = Modifier.toString(
        PublicPenguin_Crate.class.getDeclaredMethod("builder").getModifiers());
    assertThat(builderMethodModifiers, not(containsString("public")));
    String setterMethodModifiers = Modifier.toString(
        PublicPenguin_Crate.class.getDeclaredMethod("foo", String.class)
            .getModifiers());
    assertThat(setterMethodModifiers, containsString("public"));
    assertThat(setterMethodModifiers, not(containsString("final")));
  }

  @Test
  public void testStep() throws Exception {
    String classModifiers = Modifier.toString(
        PublicPenguin_Crate.Foo.class.getModifiers());
    assertThat(classModifiers, containsString("public"));
    String getMethodModifiers = Modifier.toString(
        PublicPenguin_Crate.Foo.class.getDeclaredMethod("get").getModifiers());
    assertThat(getMethodModifiers, containsString("public"));
    String stepMethodModifiers = Modifier.toString(
        PublicPenguin_Crate.Foo.class.getDeclaredMethod("bar", String.class)
            .getModifiers());
    assertThat(stepMethodModifiers, containsString("public"));
  }

}