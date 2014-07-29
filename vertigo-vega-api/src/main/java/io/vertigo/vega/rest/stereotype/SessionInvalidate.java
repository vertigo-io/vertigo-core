package io.vertigo.vega.rest.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
	 * Invalidate session (so logout).
	 * (done AFTER request, so there is a session created)
	 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionInvalidate {
	//
}
