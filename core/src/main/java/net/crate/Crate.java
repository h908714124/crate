package net.crate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for the
 * <a href="https://github.com/h908714124/crate">crate</a>
 * annotation processor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Crate {
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface Constructor {
    }
}
