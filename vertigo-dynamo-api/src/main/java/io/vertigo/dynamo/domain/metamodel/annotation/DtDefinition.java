package io.vertigo.dynamo.domain.metamodel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gestion des DtDéfinitions.
 *
 * @author  pchretien, evernat
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.TYPE })
public @interface DtDefinition {
	/**
	 * Persistance du champ.
	 */
	boolean persistent() default true;
}
