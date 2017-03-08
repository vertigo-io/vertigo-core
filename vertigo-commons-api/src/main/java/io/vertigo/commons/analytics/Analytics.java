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
	 * The category where the traced process will be stored.
	 * @return the category of the process
	 */
	String category();

	/**
	 * The name of the process being traced.
	 * @return name of process
	 */
	String name() default "";

}
