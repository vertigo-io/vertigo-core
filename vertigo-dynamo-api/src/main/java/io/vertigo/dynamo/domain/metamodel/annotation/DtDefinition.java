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
 * @version $Id: DtDefinition.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
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
