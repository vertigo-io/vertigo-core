package io.vertigo.core.component.proxy.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.component.proxy.ProxyMethodAnnotation;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ProxyMethodAnnotation

/**
 * This annotation is used to mark a method that will be proxied.
 *
 * @author pchretien
 */
public @interface AggregatorAnnotation {

	/**
	 * @return the operation that will be applied to the args of the method
	 */
	AggregatorOperation operation();
}
