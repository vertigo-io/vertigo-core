package io.vertigo.dynamo.domain.metamodel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pchretien
 * @version $Id: Field.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Field {
	/**
	 * Type du champ.
	 */
	String type() default "DATA";

	/**
	 * Nom du domaine.
	 */
	String domain();

	/**
	 * Si le champ est non null.
	 */
	boolean notNull() default false;

	/**
	 * Libellé du champ.
	 */
	String label();

	/**
	 * Persistance du champ.
	 */
	boolean persistent() default true;
}
