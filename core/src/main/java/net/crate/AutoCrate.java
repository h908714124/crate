package net.crate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *   Marker annotation for the
 *   <a href="https://github.com/h908714124/crate">crate</a>
 *   annotation processor.
 * </p>
 *
 * <p>
 *   {@code @AutoCrate} only works for classes that also have an
 *   {@code @AutoValue} annotation, and are <em>regular</em>
 *   auto-value classes, not the <em>builder</em> variety.
 * </p>
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoCrate {
}
