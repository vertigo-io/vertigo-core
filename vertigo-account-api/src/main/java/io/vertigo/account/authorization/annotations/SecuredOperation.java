package io.vertigo.account.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.component.aop.AspectAnnotation;

/**
 * This annotation must be inserted on parameter that need a secure check by OperationName.
 * Method must be annoted by @Secured
 * @author npiedeloup
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@AspectAnnotation
public @interface SecuredOperation {

	/**
	 * Returns the security configuration attributes (e.g. Operation READ, Operation WRITE).
	 *
	 * @return String The secure method attribute
	 */
	public String value();
}
