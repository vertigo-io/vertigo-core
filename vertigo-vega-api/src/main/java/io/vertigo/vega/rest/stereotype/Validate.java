package io.vertigo.vega.rest.stereotype;

import io.vertigo.vega.rest.validation.DtObjectValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
	Class<? extends DtObjectValidator>[] value();
}
