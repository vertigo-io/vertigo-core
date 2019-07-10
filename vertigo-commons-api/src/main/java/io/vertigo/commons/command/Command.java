package io.vertigo.commons.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for creating commands.
 * @author mlaroche
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	/**
	 * The handle to call the command.
	 * @return the handle
	 */
	String handle();

	String description() default "";

}
