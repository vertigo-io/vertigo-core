package io.vertigo.app.config.discovery;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * For ignoring the class with discovery.
 * @author mlaroche
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface NotDiscoverable {
	// nothing
}
