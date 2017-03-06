package io.vertigo.commons.analytics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.component.aop.AspectAnnotation;

/**
 * Annotation for the analytics Aspect.
 * @author mlaroche
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@AspectAnnotation
public @interface Analytics {
	/**
	 * The type of process being tracked.
	 * @return type of process
	 */
	String processType();

	/**
	 * The category of the process being tracked.
	 * @return category of process
	 */
	String category();
}
