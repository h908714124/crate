package net.crate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marker annotation for the
 * <a href="https://github.com/h908714124/crate">crate</a>
 * annotation processor.
 * </p>
 * <p>
 * <p>
 * <em>Important: The generated code
 * uses <a href="https://github.com/google/auto">auto-value</a>.
 * Please ensure that auto-value is configured correctly.</em>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Crate {

  @Target(ElementType.CONSTRUCTOR)
  @Retention(RetentionPolicy.SOURCE)
  @interface Constructor {
  }
}
