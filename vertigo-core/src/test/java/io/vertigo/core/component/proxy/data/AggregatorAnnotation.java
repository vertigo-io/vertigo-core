package io.vertigo.core.component.proxy.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.component.proxy.ProxyMethodAnnotation;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ProxyMethodAnnotation
public @interface AggregatorAnnotation {

	AggregatorOperation operation();
}
