package io.vertigo.core.component.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark aspect's annotations for usage check
 * @author mlaroche
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AspectAnnotation {
	//
}
