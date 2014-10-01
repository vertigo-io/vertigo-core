package io.vertigo.struts2.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate than this action can be call by a GET request.
 * By default only first access (execute method on Action class) is accepted in GET request, 
 * other method on Action class must be called with POST request.
 * @author npiedeloup
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GET {
	//nothing	
}
