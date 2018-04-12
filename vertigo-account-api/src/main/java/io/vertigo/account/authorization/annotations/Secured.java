package io.vertigo.account.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.component.aop.AspectAnnotation;

/**
 * This annotation must be inserted on methods and classes that need a secure check by AuthorizationName.
 * @author npiedeloup
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@AspectAnnotation
public @interface Secured {

	/**
	 * Returns the security configuration attributes (e.g. ROLE_USER, ROLE_ADMIN).
	 *
	 * @return String The secure method attribute
	 */
	public String value();
}
