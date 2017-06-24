package net.crate.examples;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Modifier;
import org.junit.Test;

public class PackagePiranhaTest {

  @Test
  public void testCrate() throws Exception {
    String classModifiers = Modifier.toString(
        PackagePiranha_Crate.class.getModifiers());
    assertThat(classModifiers, not(containsString("public")));
    assertThat(classModifiers, containsString("final"));
    String builderMethodModifiers = Modifier.toString(
        PackagePiranha_Crate.class.getDeclaredMethod("builder").getModifiers());
    assertThat(builderMethodModifiers, not(containsString("public")));
    String setterMethodModifiers = Modifier.toString(
        PackagePiranha_Crate.class.getDeclaredMethod("foo", String.class)
            .getModifiers());
    assertThat(setterMethodModifiers, not(containsString("public")));
    assertThat(setterMethodModifiers, not(containsString("final")));
  }

  @Test
  public void testStep() throws Exception {
    String classModifiers = Modifier.toString(
        PackagePiranha_Crate.Foo.class.getModifiers());
    assertThat(classModifiers, not(containsString("public")));
    String getMethodModifiers = Modifier.toString(
        PackagePiranha_Crate.Foo.class.getDeclaredField("foo").getModifiers());
    assertThat(getMethodModifiers, not(containsString("public")));
    String stepMethodModifiers = Modifier.toString(
        PackagePiranha_Crate.Foo.class.getDeclaredMethod("bar", String.class)
            .getModifiers());
    assertThat(stepMethodModifiers, not(containsString("public")));
  }
}